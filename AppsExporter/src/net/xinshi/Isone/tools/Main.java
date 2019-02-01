package net.xinshi.Isone.tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Main {
    final static String deployTemplate = "<exec executable=\"java\">\n" +
            "    <arg line=\"-jar \"></arg>\n" +
            "    <arg path=\"lib/appDeployer.jar\"/>\n" +
            "    <arg line=\"${deployUrl}\"></arg>\n" +
            "    <arg line=\"${deployPass}\"></arg>\n" +
            "    <arg line=$zipFile/>\n" +
            "    <arg line=$appName/>\n" +
            "    <arg line=$appId/>\n" +
            "    </exec>\n";

    public static void main(String[] args) throws Exception {
	// write your code here
        URL url = new URL(args[0]);
        InputStream in = url.openStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[2048];
        int n = 0;
        while((n=in.read(buf))!=-1){
            bos.write(buf,0,n);
        }
        in.close();
        String s = bos.toString("UTF-8");
        JSONArray jsonApps = new JSONArray(s);
        StringBuffer blocks = new StringBuffer();
        for(int i=0; i<jsonApps.length();i++){
            JSONObject jApp = (JSONObject) jsonApps.get(i);
            String zipUrl = jApp.getString("url");
            String appId = jApp.getString("id");
            String zipFileName = appId + ".zip";
            String appName = jApp.getString("name");

            String block = deployTemplate.replace("$zipFile","\"" + zipFileName + "\"");
            block = block.replace("$appName","\"" + appName + "\"");
            block = block.replace("$appId","\"" + appId + "\"");
            blocks.append(block);

            File zipFile = new File("exported",zipFileName);
            FileOutputStream fos = new FileOutputStream(zipFile);
            url = new URL(zipUrl);
            in = url.openStream();
            while((n=in.read(buf))!=-1){
                fos.write(buf,0,n);
            }
            fos.close();
            in.close();

        }
        File deployXml = new File("exported","deploy.xml");
        FileOutputStream fos = new FileOutputStream(deployXml);
        fos.write(blocks.toString().getBytes("utf-8"));
        fos.close();
    }
}
