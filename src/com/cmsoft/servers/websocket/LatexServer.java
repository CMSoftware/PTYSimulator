package com.cmsoft.servers.websocket;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class LatexServer extends Thread {
    //Logger log = LoggerFactory.getLogger(Debuger.class);
    private final Charset charset;
    private int bufferSize = 1024;
    private final Consumer<String> stdout;
    private final Consumer<Integer> doneHandler;
    //private Status status;
    private Process process;
    private final String filename;
    private boolean interrupted;

    public LatexServer(Consumer<String> stdout, Consumer<Integer> doneHandler, String filename) {
        this.charset = StandardCharsets.UTF_8;
        this.stdout = stdout;
        this.doneHandler = doneHandler;
        this.filename = filename;
        this.interrupted = false;
    }

    public Process getProcess() {
        return process;
    }

    public void interruptProcess() {
        if (!this.interrupted) {
            this.interrupted = true;
            this.process.destroy();
        }
    }

    public void run() {
        int exitValue = 0;
        ProcessBuilder pb = new ProcessBuilder("/home/yxm/workspace/test01/Debug/test01");
        try {
            //pb.directory(new File("/tmp"));
            pb.redirectInput();
            process = pb.start();
            LatexServer.Pipe stdout = new LatexServer.Pipe(new BufferedReader(new InputStreamReader(process.getInputStream())));
            LatexServer.Pipe stderr = new LatexServer.Pipe(new BufferedReader(new InputStreamReader(process.getErrorStream())));
            Thread stdin = new Thread() {
                InputStreamReader in = new InputStreamReader(System.in);
                @Override
                public void run() {
                    int ch;
                    while(true) {
                        try {
                            if (-1 != (ch = in.read())) {
                                LatexServer.this.process.getOutputStream().write(ch);
                                if('\n' == ch) {
                                    LatexServer.this.process.getOutputStream().flush();
                                }
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                }
            };

            stdout.start();
            stderr.start();
            stdin.setDaemon(true);
            stdin.start();

            try {
                //this.log.debug("Waiting stdout to complete...");
                stdout.join();
                //this.log.debug("Stdout completed.");
            } catch (InterruptedException e) {
                //this.setStatus(Status.INTERRUPTED);
                Thread.currentThread().interrupt();
            }

            try {
                stderr.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("over.");
        } catch (IOException e) {
            this.stdout.accept("Error!");
        }
        this.doneHandler.accept(exitValue);
    }

    private class Pipe extends Thread {
        private final BufferedReader in;

        public Pipe(BufferedReader bufferedReader) {
            this.in = bufferedReader;
        }

        public void run() {
            int a;
            byte[] buffer = new byte[LatexServer.this.bufferSize];
            int len = 0;
            while(true) {
                try {
                    if (-1 != (a = this.in.read())) {
                        LatexServer.this.stdout.accept(Character.toString(a));
                    } else {
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
