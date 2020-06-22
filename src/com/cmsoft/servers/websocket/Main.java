package com.cmsoft.servers.websocket;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) throws InterruptedException {
	    //System.out.println(latexmk("/tmp/t2.tex").toString());

	    LatexServer server = new LatexServer(
                (a) -> {
                    System.out.print(a);
                    },
                (a) -> {
                    if(null == a) {
                        System.out.println("Completed without any error.");
                    } else {
                        System.out.println("Complete with error:" + a);
                    }
                    },
                "/tmp/t2.tex"
        );
	    server.start();
    }

    public static Object latexmk(String filename) {
        List<String> command = new ArrayList<String>();
        //command.add("latexmk");  // /opt/texlive/2020/bin/x86_64-linux/
        command.add("xelatex");
        command.add("-interaction=nonstopmode");
        command.add(filename);
        ProcessBuilder pb = new ProcessBuilder(command); //"/bin/bash"
        pb.directory(new File("/tmp"));
        pb.redirectError(new File("/tmp/error.txt"));
        try {
            Process process = pb.start();
            pb.redirectErrorStream(true);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            int flag = 0;
            if(process.isAlive()) {
                String readLine = null;
                while (2 != flag) {
                    if (null != (readLine = br.readLine())) {
                        System.out.println(readLine);
                        flag = 0;
                    } else {
                        flag = 1;
                    }
                    if(null != (readLine = error.readLine())) {
                        System.out.println(readLine);
                        flag -= 1;
                    } else {
                        flag += 1;
                    }
                }
                process.waitFor();
            }
        } catch (Exception e) {
            command.clear();
            command.add(e.getMessage());
        }
        return command;
    }
}
