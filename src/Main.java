import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        String desktopDirectory = System.getProperty("user.home") + "\\Desktop";
        String botToken = "5195909558:AAHeO85DtxK_j09Y3i4cM1KkE4obSr7ng9w";
        String chat_id = "-1001760421137";
        String lpuName = "my_lpu";
        String archivation = "winrar";//winrar 7z no
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
        writer.write("Taskkill /IM SQLiteStudio.exe /F" + "\n");

        writer.write("set /a db_counter = 0 \n");
        writer.write("set /a vacuumed_counter = 0 \n");

        for (int i = 0; i < dbPaths.size(); i++) {
            writer.write("echo --------------------------" + dbNames.get(i).toUpperCase() + "-------------------------- >> " + logPath + "\n");

            writer.write("cd /d " + dbDirectories.get(i) + "\n");
            //начинаем процесс бекапа и вакуума только если база использовалась давно

            writer.write("set /a db_counter += 1 \n");
            writer.write("set /a vacuumed_counter += 1 \n");
            writer.write("set N=60" + "\n");
            writer.write("set file=" + dbPaths.get(i) + "\n");
            writer.write("set /a const=%N%*60" + "\n");
            writer.write("for /f \"usebackq delims=\" %%I in ('\"%file%\"') do set A=%%~tI" + "\n");
            writer.write("set mon=%a:~3,2%" + "\n");
            writer.write("set D=%a:~0,2%" + "\n");
            writer.write("set h=%a:~11,2%" + "\n");
            writer.write("set m=%a:~14,2%" + "\n");
            writer.write("set /a Hs=H*3600" + "\n");
            writer.write("set /a Ms=M*60" + "\n");
            writer.write("set /a Ts=Hs+Ms" + "\n");
            writer.write("set now=%DATE: =0% %TIME: =0%" + "\n");
            writer.write("set mon1=%now:~3,2%" + "\n");
            writer.write("set D1=%now:~0,2%" + "\n");
            writer.write("set H1=%now:~11,2%" + "\n");
            writer.write("set M1=%now:~14,2%" + "\n");
            writer.write("set /a Hs1=H1*3600" + "\n");
            writer.write("set /a Ms1=M1*60" + "\n");
            writer.write("set /a Ts1=Hs1+Ms1" + "\n");
            writer.write("if %mon1% GTR %mon% (set /a Mons=D*86400) else set Mons=0" + "\n");
            writer.write("set /a TimeI=Ts+mons" + "\n");
            writer.write("set /a y=d+d1" + "\n");
            writer.write("if %mon1% GTR %mon% (set /a Mons1=y*86400) else set Mons1=0" + "\n");
            writer.write("If %D1% GTR %D% (set Ds1=86400) else set Ds1=0" + "\n");
            writer.write("set /a TimeT=Ts1+Ds1+Mons1-const-TimeI" + "\n");
            writer.write("set /a mm=TimeT/60" + "\n");
            writer.write("set /a n2=mm+n" + "\n");
            writer.write("set /a n1=timet+n*60" + "\n");
            writer.write("if %n2% GEQ %N% (" + "\n");

            writer.write("echo The difference is more than %n% minutes, starting vacuumization! >> " + logPath + "\n");
            writer.write("echo 1. A new vacuumization started %date% at %time% >> " + logPath + "\n");
            if (archivation == "winrar") {
                writer.write("if exist " + dbNames.get(i)+ "_backup_*.rar forfiles /p " + dbDirectories.get(i) + " /m " + dbNames.get(i) + "_backup_*.rar /d -32 /c \"cmd /c del @file\"\n");
            } else if (archivation == "7z") {
                writer.write("if exist " + dbNames.get(i)+ "_backup_*.7z forfiles /p " + dbDirectories.get(i) + " /m " + dbNames.get(i) + "_backup_*.7z /d -32 /c \"cmd /c del @file\"\n");
            } else if (archivation == "no") {
                writer.write("if exist " + dbNames.get(i) + "_backup_*.db forfiles /p " + dbDirectories.get(i) + " /m " + dbNames.get(i) + "_backup_*.db /d -32 /c \"cmd /c del @file\"\n");
            }
            //очистка архивов
            writer.write("if exist " + dbNames.get(i)+ "_archive_*.db forfiles /p " + dbDirectories.get(i) + " /m " + dbNames.get(i) + "_archive_*.db /d -5 /c \"cmd /c del @file\"\n");
            writer.write("echo 2. The old backups and archives were deleted at %time% >>  " + logPath + "\n");
            writer.write("ren " + dbNames.get(i) + ".db " + dbNames.get(i) + "_copy.db" + "\n");
            writer.write("sqlite3 \"" + dbNames.get(i) + "_copy.db\" \".backup '" + dbNames.get(i) + "_backup_%date%.db'\"" + "\n");
            writer.write("echo 3. A new backup was created at %time% >> " + logPath + "\n");
            //архивация бэкапа, если есть архиватор
            if (archivation == "winrar") {
                writer.write("\"" + archiverPath + "\\winrar.exe" + "\" a " + dbNames.get(i) + "_backup_%date%.rar " + dbNames.get(i) + "_backup_%date%.db" + "\n");
                writer.write("if exist " + dbNames.get(i) + "_backup_%date%.rar del " + dbNames.get(i) + "_backup_%date%.db" + "\n");
            } else if (archivation == "7z") {
                writer.write("\"" + archiverPath + "\\7z.exe" + "\" a " + dbNames.get(i) + "_backup_%date%.7z " + dbNames.get(i) + "_backup_%date%.db" + "\n");
                writer.write("if exist " + dbNames.get(i) + "_backup_%date%.7z del " + dbNames.get(i) + "_backup_%date%.db" + "\n");
            }
            writer.write("if exist " + dbNames.get(i) + "_vacuumed.db del " + dbNames.get(i) + "_vacuumed.db" + "\n");
            writer.write("sqlite3 \"" + dbNames.get(i) + "_copy.db\" \"VACUUM INTO '" + dbNames.get(i) + "_vacuumed.db'\"" + "\n");
            writer.write("echo 4. The base was vacuumized at %time% >> " + logPath + "\n");
            writer.write("echo 5. The old base was renamed %time% >> " + logPath + "\n");
            writer.write("if exist " + dbNames.get(i) + ".db " + "del " + dbNames.get(i) + ".db  " + "\n");
            writer.write("if exist " + dbNames.get(i) + "_vacuumed.db " + "ren " + dbNames.get(i) + "_vacuumed.db " + dbNames.get(i) +".db\n");
            writer.write("echo 6. The vacuumized base was renamed to the main at %time% >> " + logPath + "\n");
            writer.write("if exist " + dbNames.get(i) + ".db del " + dbNames.get(i) + "_copy.db\n");
            writer.write("if exist " + dbNames.get(i) + "_vacuumed.db (\n");
            writer.write("echo ALERT The base " + dbNames.get(i) + ", number " + i + " at LPU " + lpuName + " wasn't completely vacuumed! >> " + logPath + "\n");
            writer.write("cd /d " + curlPath + " \n");
            writer.write("curl https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chat_id + "^^^&text=" + "\"Database " + dbNames.get(i) + ", number " + i + ", at LPU " + lpuName + " wasn't completely vacuumed!\" \n");
            writer.write("set /a vacuumed_counter -= 1 )\n");
            writer.write("echo 7. The old base was deleted %time% >> " + logPath + "\n");
            writer.write(") else ( \n" +
                    "echo The difference is less than %n% minutes, vacuumization does not begin >> " + logPath + "\n");
            writer.write("set /a vacuumed_counter -= 1 )\n");
            writer.write("echo ------------------------------------------------------------- >> " + logPath + "\n" + "\n");
        }
        writer.write("cd /d " + curlPath + "\n");
        writer.write("set message=\"%vacuumed_counter% out of %db_counter% bases at LPU " + lpuName + " were vacuumized!\"" + "\n");
        writer.write("echo %vacuumed_counter% out of %db_counter% bases were vacuumized! >> " + logPath + "\n");
        writer.write("curl https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chat_id + "^^^&text=%message%"+ "\n");
        writer.flush();
        writer.close();
        System.out.println("Для баз ЛПУ " + lpuName + " сгенерирован батник!");
    }
}