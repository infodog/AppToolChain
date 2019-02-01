package net.xinshi.importer;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: mac
 * Date: 13-7-15
 * Time: 下午11:38
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    static public void main(String[] args) throws Exception {
        List<String> libs = new ArrayList();
        String inFile = null;
        String outFile = null;
        int i = 0;
        while (i < args.length) {
            String s = args[i];
            if(s.equals("-libs")){
                String libArgs = args[i+1];
                String[] libArrays = libArgs.split(";");
                for(String libPath : libArrays){
                    File f = new File(libPath);
                    if(f.isDirectory() && f.exists()){
                        libs.add(f.getCanonicalPath());
                        System.out.println("jsLib : " + f.getCanonicalPath());
                    }
                    else{
                        throw new Exception("jsLib : " + f.getCanonicalPath() + " does not exists.");
                    }
                }

                i+=2;
            }
            if(s.equals("-in")){
                inFile = args[i+1];
                i+=2;
            }
            if(s.equals("-out")){
                outFile = args[i+1];
                i+=2;
            }
        }
        if(inFile == null){
            System.out.println("input file must not null.");
            System.out.print("import -libs <libs> -in <inFile> -out <outFile>");
            return;
        }
        doImport(inFile,outFile,libs);
    }

    static void doImport(String inFile,String outFile,List<String> serverLibs) throws Exception {
        Map<String, String> import_files = new HashMap<String, String>();
        StringBuilder out = new StringBuilder();
        File curFile = new File(inFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(curFile),"utf-8"));
        importFromReader(out, reader, serverLibs, curFile, import_files);
        File outputFile = new File(outFile);
        outputFile.getCanonicalFile().getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(outputFile.getCanonicalFile());
        fos.write(out.toString().getBytes("UTF-8"));
    }

    static boolean isAbsolute(String path) {
        if (path.startsWith("/")) {
            return true;
        }
        if (path.length() > 2) {
            String s = path.substring(1, 2);
            if (s.equals(":")) {
                return true;
            }
        }
        return false;
    }

    static void includeFile(StringBuilder builder, List<String> serverLibs, String fileName, File currentFile, Map<String, String> import_files) throws Exception {
        File f = null;
        if (isAbsolute(fileName)) {
            f = new File(fileName);
        } else {
            if (currentFile != null) {
                f = new File(currentFile.getParent(), fileName);
            }
            //在当前文件找不到，就对于标准库进行搜索
            if (!(f != null && f.exists() && f.isFile())) {
                for (String serverLib : serverLibs) {
                    f = new File(serverLib, fileName);
                    if (f.exists() && f.isFile()) {
                        break;
                    }
                }
            }
        }

        if (f.exists() && f.isFile()) {
            String file_path = f.getCanonicalPath();
            if (import_files.get(file_path) != null) {
                //System.out.println("script file has imported : " + file_path);
                return;
            }
            System.out.println("importing " + file_path);
            import_files.put(file_path, file_path);

            FileInputStream fin = new FileInputStream(f);
            InputStreamReader ir = new InputStreamReader(fin, "utf-8");
            BufferedReader reader = new BufferedReader(ir);
            importFromReader(builder, reader, serverLibs, f,  import_files);
            reader.close();
        }
        else{
            throw new Exception("File not found:" + f.getCanonicalPath());
        }
    }

    static void importFromReader(StringBuilder out, BufferedReader reader, List<String> serverLibs, File currentFile,  Map import_files) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line!=null && line.startsWith("//#import")) {
                String filePath = line.substring("//#import".length());
                filePath = filePath.trim();
                includeFile(out, serverLibs, filePath, currentFile, import_files);

            } else {
                out.append(line).append("\n");
            }
        }
    }
}
