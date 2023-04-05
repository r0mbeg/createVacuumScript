import java.io.*;
import java.util.ArrayList;

public class Vacuum extends bot_config{

    public static void main(String[] args) throws IOException {
        String desktopDirectory = System.getProperty("user.home") + "\\Desktop";

        String lpuName = "d58";
        String archivation = "7z";//winrar 7z no

        ArrayList<String> inputList = new ArrayList<>();
        try {
            File inputFile = new File(desktopDirectory + "\\input.txt");
            FileReader fR = new FileReader(inputFile);
            BufferedReader reader = new BufferedReader(fR);
            String line = reader.readLine();
            while (line != null) {
                inputList.add(line);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String logPath = inputList.get(0);
        String curlPath = inputList.get(1);
        String archiverPath = inputList.get(2);
        ArrayList<String> dbPaths = new ArrayList<>();
        for (int i = 3; i < inputList.size(); i++) {
            dbPaths.add(inputList.get(i));
        }
        ArrayList<String> dbNames = new ArrayList<>();
        ArrayList<String> dbDirectories = new ArrayList<>();
        for (int i = 0; i < dbPaths.size(); i++) {
            dbNames.add(dbPaths.get(i).substring(dbPaths.get(i).lastIndexOf('\\') + 1, dbPaths.get(i).indexOf('.') ));
            dbDirectories.add(dbPaths.get(i).substring(0, dbPaths.get(i).lastIndexOf('\\')));
        }
        File file = new File(desktopDirectory + "\\vacuum_" + lpuName + ".bat");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);

        writer.write("cd /d C:\\Windows\\System32" + "\n");
        writer.write("iisreset /stop" + "\n");
        writer.write("Taskkill /IM SQLiteStudio.exe /F" + "\n");
        writer.write("set /a res = 1\n");
        writer.write("set /a db_counter = 0 \n");
        writer.write("set /a vacuumed_counter = 0 \n");
        for (int i = 0; i < dbPaths.size(); i++) {
            writer.write("echo --------------------------" + dbNames.get(i).toUpperCase() + "-------------------------- >> " + logPath + "\n");
            writer.write("cd /d " + dbDirectories.get(i) + "\n");
            //начинаем процесс бекапа и вакуума только если база использовалась давно
            writer.write("set /a res = 1\n");
            writer.write("set /a db_counter += 1 \n");

            writer.write("if exist sqlite3.exe ( \n");
            writer.write("set /a vacuumed_counter += 1 \n");
            writer.write("echo 1. A new vacuumization started %date% at %time% >> " + logPath + "\n");
            writer.write("if exist " + dbNames.get(i)+ "_backup_*.rar forfiles /p " + dbDirectories.get(i) + " /m " + dbNames.get(i) + "_backup_*.rar /d -32 /c \"cmd /c del @file\"\n");
            writer.write("if exist " + dbNames.get(i)+ "_backup_*.7z forfiles /p " + dbDirectories.get(i) + " /m " + dbNames.get(i) + "_backup_*.7z /d -32 /c \"cmd /c del @file\"\n");
            writer.write("if exist " + dbNames.get(i) + "_backup_*.db forfiles /p " + dbDirectories.get(i) + " /m " + dbNames.get(i) + "_backup_*.db /d -32 /c \"cmd /c del @file\"\n");
            writer.write("if exist " + dbNames.get(i)+ "_archive_*.db forfiles /p " + dbDirectories.get(i) + " /m " + dbNames.get(i) + "_archive_*.db /d -5 /c \"cmd /c del @file\"\n");
            writer.write("echo 2. The old backups and archives were deleted at %time% >>  " + logPath + "\n");
            writer.write("ren " + dbNames.get(i) + ".db " + dbNames.get(i) + "_copy.db" + "\n");
            writer.write("sqlite3 \"" + dbNames.get(i) + "_copy.db\" \".backup '" + dbNames.get(i) + "_backup_%date%.db'\"" + "\n");
            writer.write("for %%i in ("+ dbNames.get(i) + "_backup_%date%.db) do if %%~zi LSS 5000 ( set /a res = 0)" + "\n");
            writer.write("echo 3. A new backup was created at %time% >> " + logPath + "\n");
            writer.write("if exist " + dbNames.get(i) + "_vacuumed.db del " + dbNames.get(i) + "_vacuumed.db" + "\n");
            writer.write("sqlite3 \"" + dbNames.get(i) + "_copy.db\" \"VACUUM INTO '" + dbNames.get(i) + "_vacuumed.db'\"" + "\n");
            writer.write("for %%i in ("+ dbNames.get(i) + "_vacuumed.db) do if %%~zi LSS 5000 ( set /a res = 0)"+ "\n");
            writer.write("echo 4. The base was vacuumized at %time% >> " + logPath + "\n");
            writer.write("echo 5. The old base was renamed %time% >> " + logPath + "\n");
            writer.write("if exist " + dbNames.get(i) + ".db " + "del " + dbNames.get(i) + ".db  " + "\n");
            writer.write("if exist " + dbNames.get(i) + "_vacuumed.db " + "ren " + dbNames.get(i) + "_vacuumed.db " + dbNames.get(i) +".db\n");
            writer.write("echo 6. The vacuumized base was renamed to the main at %time% >> " + logPath + "\n");
            writer.write("if exist " + dbNames.get(i) + ".db del " + dbNames.get(i) + "_copy.db\n");
            writer.write("if exist " + dbNames.get(i) + "_vacuumed.db set /a res = 0\n");
            writer.write("if exist " + dbNames.get(i) + "_copy.db set /a res = 0\n");
            writer.write("if %res% == 0 ( \n");
            writer.write("echo ALERT The base " + dbNames.get(i) + ", number " + i + " at LPU " + lpuName + " wasn't completely vacuumed! >> " + logPath + "\n");
            writer.write("cd /d " + curlPath + " \n");
            writer.write("curl https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chat_id + "^^^&text=" + "\"Database " + dbNames.get(i) + ", number " + i + ", at LPU " + lpuName + " wasn't completely vacuumed!\" \n");
            writer.write("set /a vacuumed_counter -= 1 )\n");
            writer.write("echo 7. The old base was deleted %time% >> " + logPath + "\n");
            writer.write(")\n");
            writer.write("echo ------------------------------------------------------------- >> " + logPath + "\n" + "\n");
        }

        writer.write("cd /d C:\\Windows\\System32" + "\n");
        writer.write("iisreset /start" + "\n");


        writer.write("echo -----------------BACKUPS-ARCHIVATION-STARTS-------------------- >> " + logPath + "\n");
        for (int i = 0; i < dbPaths.size(); i ++) {
            writer.write("echo  Archivation of the base " + dbNames.get(i) + ", number " + i +" started %date% at %time% >> " + logPath + "\n");
            writer.write("cd /d " + dbDirectories.get(i) + "\n");
            if (archivation == "winrar") {
                writer.write("\"" + archiverPath + "\\winrar.exe" + "\" a " + dbNames.get(i) + "_backup_%date%.rar " + dbNames.get(i) + "_backup_%date%.db" + "\n");
                writer.write("if exist " + dbNames.get(i) + "_backup_%date%.rar del " + dbNames.get(i) + "_backup_%date%.db" + "\n");
            } else if (archivation == "7z") {//лучше!!!
                writer.write("\"" + archiverPath + "\\7z.exe" + "\" a " + dbNames.get(i) + "_backup_%date%.7z " + dbNames.get(i) + "_backup_%date%.db" + "\n");
                writer.write("if exist " + dbNames.get(i) + "_backup_%date%.7z del " + dbNames.get(i) + "_backup_%date%.db" + "\n");
            }
            writer.write("echo  Archivation of the base " + dbNames.get(i) + ", number " + i +" ended %date% at %time% >> " + logPath + "\n");
        }
        writer.write("echo -----------------BACKUPS-ARCHIVATION-ENDS-------------------- >> " + logPath + "\n" + "\n");


        writer.write("cd /d " + curlPath + "\n");
        writer.write("set message=\"%vacuumed_counter% out of %db_counter% bases at LPU " + lpuName + " were vacuumized!\"" + "\n");
        writer.write("echo %vacuumed_counter% out of %db_counter% bases were vacuumized! >> " + logPath + "\n");
        writer.write("curl https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chat_id + "^^^&text=%message%"+ "\n");


        writer.flush();
        writer.close();
        System.out.println("Для баз ЛПУ " + lpuName + " сгенерирован батник!");
    }
}