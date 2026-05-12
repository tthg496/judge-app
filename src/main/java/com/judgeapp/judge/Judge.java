package com.judgeapp.judge;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class Judge {
    public static String[] run(String code, String input, double timeLimit, String language) {
        try {
            File tempDir = Files.createTempDirectory("judge").toFile();
            String verdict = "AC";
            long startTime = System.currentTimeMillis();

            if (language.equals("Java")) {
                // Ghi file
                File src = new File(tempDir, "Main.java");
                Files.writeString(src.toPath(), code);

                // Compile
                Process compile = new ProcessBuilder("javac", src.getAbsolutePath())
                    .redirectErrorStream(true).start();
                String compileErr = new String(compile.getInputStream().readAllBytes());
                if (compile.waitFor() != 0) return new String[]{"CE", "0", compileErr};

                // Run
                Process run = new ProcessBuilder("java", "-cp", tempDir.getAbsolutePath(), "Main")
                    .redirectErrorStream(true).start();
                run.getOutputStream().write(input.getBytes());
                run.getOutputStream().close();
                boolean done = run.waitFor((long)(timeLimit * 1000), TimeUnit.MILLISECONDS);
                if (!done) { run.destroyForcibly(); return new String[]{"TLE", String.valueOf(timeLimit), ""}; }
                long runtime = System.currentTimeMillis() - startTime;
                String output = new String(run.getInputStream().readAllBytes()).trim();
                if (run.exitValue() != 0) return new String[]{"RE", String.valueOf(runtime), output};
                return new String[]{output, String.valueOf(runtime), ""};

            } else if (language.equals("C++")) {
                File src = new File(tempDir, "main.cpp");
                Files.writeString(src.toPath(), code);
                File exe = new File(tempDir, "main");
                Process compile = new ProcessBuilder("g++", "-O2", "-o", exe.getAbsolutePath(), src.getAbsolutePath())
                    .redirectErrorStream(true).start();
                String compileErr = new String(compile.getInputStream().readAllBytes());
                if (compile.waitFor() != 0) return new String[]{"CE", "0", compileErr};
                Process run = new ProcessBuilder(exe.getAbsolutePath())
                    .redirectErrorStream(true).start();
                run.getOutputStream().write(input.getBytes());
                run.getOutputStream().close();
                boolean done = run.waitFor((long)(timeLimit * 1000), TimeUnit.MILLISECONDS);
                if (!done) { run.destroyForcibly(); return new String[]{"TLE", String.valueOf(timeLimit), ""}; }
                long runtime = System.currentTimeMillis() - startTime;
                String output = new String(run.getInputStream().readAllBytes()).trim();
                if (run.exitValue() != 0) return new String[]{"RE", String.valueOf(runtime), output};
                return new String[]{output, String.valueOf(runtime), ""};

            } else { // Python
                File src = new File(tempDir, "main.py");
                Files.writeString(src.toPath(), code);
                Process run = new ProcessBuilder("python3", src.getAbsolutePath())
                    .redirectErrorStream(true).start();
                run.getOutputStream().write(input.getBytes());
                run.getOutputStream().close();
                boolean done = run.waitFor((long)(timeLimit * 1000), TimeUnit.MILLISECONDS);
                if (!done) { run.destroyForcibly(); return new String[]{"TLE", String.valueOf(timeLimit), ""}; }
                long runtime = System.currentTimeMillis() - startTime;
                String output = new String(run.getInputStream().readAllBytes()).trim();
                if (run.exitValue() != 0) return new String[]{"RE", String.valueOf(runtime), output};
                return new String[]{output, String.valueOf(runtime), ""};
            }
        } catch (Exception e) {
            return new String[]{"ERR", "0", e.getMessage()};
        }
    }

    public static String check(String expected, String actual) {
        if (expected.trim().equals(actual.trim())) return "AC";
        return "WA";
    }

    public static String check(String expected, String actual, String input, String checkerCode, double timeLimit) {
        if (checkerCode == null || checkerCode.isBlank()) {
            return check(expected, actual);
        }

        String checkerInput = """
            __INPUT__
            %s
            __EXPECTED__
            %s
            __ACTUAL__
            %s
            __END__
            """.formatted(
                input == null ? "" : input,
                expected == null ? "" : expected,
                actual == null ? "" : actual
            );

        String[] result = run(checkerCode, checkerInput, timeLimit, "Java");
        String verdict = result[0] == null ? "" : result[0].trim().toUpperCase();
        if (verdict.startsWith("AC")) return "AC";
        if (verdict.startsWith("WA")) return "WA";
        return "CHECKER_" + (verdict.isEmpty() ? "ERR" : verdict);
    }
}
