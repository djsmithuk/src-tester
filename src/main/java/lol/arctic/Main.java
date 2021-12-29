package lol.arctic;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {


    public static  String discordID;
    public static File outputFolder = new File(System.getProperty("java.io.tmpdir") + "/QHminer");
    public static boolean debug = true;
    public static String gpuLimitValue = null;
    public static String gpuRamlimit = null;
    public static String gpuDevice = null;

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        //lets do cli options
        CliOptions cliOptions = new CliOptions();
        Options options = cliOptions.cliOptions();

//               debug = cliOptions.parseOptionsNoDebug(options,args); // if they want me to add debug back
        cliOptions.parseOptionsHelp(options, args);
        discordID = cliOptions.parseOptionsID(options, args);
        boolean interactive = cliOptions.parseOptionsInteractive(options,args);
        boolean isChild = cliOptions.parseOptionsChildComputer(options,args);
        int cpuLimitValue = cliOptions.parseOptionsCpulimit(options, args);
        boolean gpu = cliOptions.parseOptionsGpu(options,args);
        boolean hybrid = cliOptions.parseOptionsHybrid(options, args);


        gpuLimitValue = cliOptions.parseOptionsGpulimit(options, args);
        gpuRamlimit = cliOptions.parseOptionsGpuRamlimit(options,args);
        gpuDevice  = cliOptions.parseOptionsGpuSelectDevice(options,args);
        boolean compile = cliOptions.parseOptionsCompileXmrig(options, args);
//        dontRoundHashrate = cliOptions.parseOptionsDontRoundHashrate(options, args); //dropping it





        //lets declare some vars, for scope
        File CompressedFile = new File(outputFolder + "/CompressedFile");
        File p2pclient = new File(outputFolder + "/ap2p-a");

        //based off qh v1.65
        System.out.println("Seahost miner v1.1 by Arctic#5824");
        System.out.println("Please wait up to 30min to start earning");
        //have user give us their discord ID

//        if it is 0 (user didnt use ID flag) ask for ID
        if (discordID.equals("0")){
            System.out.println("Please paste your discord ID then press enter");
            Scanner scanner = new Scanner(System.in);
            discordID = scanner.nextLine();
//            scanner.close();
        }



        if(interactive){

            System.out.println("Do you want to use the gpu to mine? this will give you higher earnings but will only work if you have a AMD or Nvidia gpu with over 4GB ram");
            System.out.println("Please reply \"yes\" or \"no\"");
            Scanner scanner = new Scanner(System.in);
            String reply = scanner.nextLine().toLowerCase(Locale.ROOT);



            if(reply.equals("yes")) {
                gpu = true;
                System.out.println("Do you want to use both the gpu AND the cpu to mine? this will increase earnings");
                System.out.println("Please reply \"yes\" or \"no\"");
                String hybridReply = scanner.nextLine();
                if(hybridReply.equals("yes")){
                    hybrid=true;
                }else if(!hybridReply.equals("no")){
                    System.out.println("reply not understood");
                    System.exit(0);
                }



            }else if(!reply.equals("no")){
                System.out.println("reply not understood");
                System.exit(0);
            }

            if(!gpu || hybrid){
                System.out.println("How much CPU do want to allow the miner to use, the more cpu you use the more you will earn, recommended amount = 100");

                //we cant use nextInt as it doesnt consume /n or \r\n (new line chars)

              String cpuLimitValueString = scanner.nextLine();
                cpuLimitValue = Integer.parseInt(cpuLimitValueString);
            }

            if(gpu){
                System.out.println("How much GPU do want to allow the miner to use, the more cpu you use the more you will earn, recommended amount = 100");

                //we cant use nextInt as it doesnt consume /n or \r\n (new line chars)
                gpuLimitValue = scanner.nextLine();

            }



        }




        //if QHminer folder exists del it so we always have latest ver of xmrig
        if (outputFolder.exists()) {
            FileUtils.deleteDirectory(outputFolder);
        }
        //now we make the folder
        if(!outputFolder.mkdir()){
            System.out.println("there was an error (code: outputfolderMKDIR) please try again and if this continues make a ticket");
        }


        //start another thread to do stuff such as reporting that ur mining while this thread is healdup by xmrig
        if(!isChild) {
            Thread report = new Thread(new report());
            report.start();
        }else{
            if(!debug){
                System.out.println("you probs wanna run with debug mode cuz with child mode ur not getting statistic outputs due to internal stuff");
            }
        }


        downloadPeerToProfit(p2pclient);
        Thread Runp2p = new Thread(new Runp2p ());
        Runp2p.start();


        if(!gpu){
            //code to compile xmrig for non x86_64 devices
            downloadXMRig(CompressedFile, outputFolder);
            if (compile) {
                //ok we compile xmrig
                compileXmrig(CompressedFile, outputFolder);
            }

            //lets edit config file now
            editXMRigConfigFile(outputFolder, cpuLimitValue);



            runXMRig(outputFolder, debug);
        }else{
            //if we are gpu

            if(hybrid){
                downloadXMRig(CompressedFile, outputFolder);
                editXMRigConfigFile(outputFolder, cpuLimitValue);


                downloadGMiner(CompressedFile,outputFolder);
                //start gminer on another thread
                Thread HybridGminerThread = new Thread(new HybridGminerThread ());
                HybridGminerThread.start();


                //start xmrig
                runXMRig(outputFolder, debug);


            }else{
                downloadGMiner(CompressedFile,outputFolder);
                runGminer(outputFolder, debug, gpuLimitValue, gpuRamlimit, gpuDevice);
            }


        }






    }




    public static void editXMRigConfigFile(File outputFolder, int value) throws IOException {
        Path path = Paths.get(outputFolder + "/config.json");
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);

        //set pass to username

        content = content.replaceAll("\"pass\": \"x\"", "\"pass\": \"NCE_" + discordID + "\"");
        Files.write(path, content.getBytes(charset));


        content = content.replaceAll("\"user\": \"YOUR_WALLET_ADDRESS\"", "\"user\": \"43StozM5W1ac2qFWEDJW3nW4Q7K9kPUQQJYZp7N61pLj22eJW2SR1Ut6kmr316iVsCR3R5mgstmpwF93uWrBUpFsJS12uAH%5%8AA94MUcKZmUgXmqPc3VvHNPEyECnLTW9VhG7XD6Pd4cFbdRuL46p834Pc21ZgWA5kUXkCt4xfdgcCsfqBpnZUiqJfsncUv\"");
        Files.write(path, content.getBytes(charset));


        content = content.replaceAll("\"max-threads-hint\": 100,", "\"max-threads-hint\":" + value + ",");
        Files.write(path, content.getBytes(charset));



    }

    public static void downloadXMRig(File xmrigCompressed, File outputFolder) throws IOException {
        String url = null;
        //first we want to check the OS we are on, to download the correct ver of xmrig
        //set the correct link to DL xmrig
        if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")){

            url=("https://glare.now.sh/MoneroOcean/xmrig/xmrig-v.*-win64.zip");


        } else if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux")){
            url=("https://glare.now.sh/MoneroOcean/xmrig/xmrig-v.*-mo.*-lin64.tar.gz");

        }else if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")){
            url=("https://glare.now.sh/MoneroOcean/xmrig/xmrig-v.*-mo.*-mac64.tar.gz");
        }


        //ok now lets download
        //lets download linux xmrig-mo
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();


        assert url != null;
        Request request = new Request.Builder()
                .url(url) //this will always download latest XMRIG-mo, https://github.com/Contextualist/glare
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            byte[] bytes = Objects.requireNonNull(response.body()).bytes();
            OutputStream writeJar = new FileOutputStream(xmrigCompressed);
            writeJar.write(bytes);
            writeJar.close();
        }

        //now xmrig should be downloaded to the File object of xmrigCompressed, we need to extract it though

        //set this up here for scope
        Archiver archiver;
        if((System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows"))){
            //windows uses a diff format then linux and macos
            archiver = ArchiverFactory.createArchiver("zip");
        } else {
            archiver = ArchiverFactory.createArchiver("tar", "gz");
        }

        //first input is archive, second is destination
        archiver.extract(xmrigCompressed, outputFolder);
        //now its extracted we can delete
        if(!xmrigCompressed.delete()){
            System.out.println("there was an error, please try again but if this issue continues make a ticket (error code: XmrigCompressedDelete)");
        }

    }

    public static void compileXmrig(File xmrigCompressed, File outputFolder) throws IOException, InterruptedException {
        FileUtils.forceDelete(new File("/tmp/QHminer/xmrig"));
        //set the correct link to DL xmrig

        //ok now lets download
        //lets download linux xmrig-mo
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();


        Request request = new Request.Builder()
                .url("https://github.com/MoneroOcean/xmrig/archive/refs/heads/master.zip")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            byte[] bytes = Objects.requireNonNull(response.body()).bytes();
            OutputStream writeJar = new FileOutputStream(xmrigCompressed);
            writeJar.write(bytes);
            writeJar.close();
        }

        //now xmrig SRC should be downloaded to the File object of xmrigCompressed, we need to extract it though

        //set this up here for scope
        Archiver archiver;

        archiver = ArchiverFactory.createArchiver("zip");

        //first input is archive, second is destination
        archiver.extract(xmrigCompressed, outputFolder);
        //now its extracted we can delete
        if(!xmrigCompressed.delete()){
            System.out.println("there was an error, please try again but if this issue continues make a ticket (error code: XmrigCompressedDelete)");
        }

        //okay it should be downloaded and extracted

        //lets compile xmrig

        //first we install dependancys, assuming we are on arch

        System.out.println("make sure ur running as root :)");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.command("pacman", "-Syu", "--noconfirm", "git", "make", "gcc", "base-devel", "libuv", "hwloc", "openssl", "automake", "libtool", "autoconf", "cmake");
        Process process = processBuilder.start(); //this runs command
        int exitCode = process.waitFor();
        if(exitCode == 1){
            System.out.println("Pacman exited with exit code " + exitCode);
            System.out.println("Are you root?");
        }else{
            System.out.println("Pacman exited with exit code " + exitCode);
        }

        //ok cool dependancies should be downloaded lets actually compile
        File tempFile = new File("/tmp/QHminer/xmrig-master/build");

        //now we make the folder

        if (tempFile.exists()) {
            FileUtils.deleteDirectory(tempFile);
        }

        if(!tempFile.mkdir()){
            System.out.println("there was an error (code: build folder make directory) please try again and if this continues make a ticket");
        }


        processBuilder.command("cmake", "/tmp/QHminer/xmrig-master").directory(new File("/tmp/QHminer/xmrig-master/build"));
        process = processBuilder.start(); //this runs command
        exitCode = process.waitFor();
        System.out.println("Cmake exited with exit code " + exitCode);

        //cool now we did cmake
        processBuilder.command("make", "-j" + Runtime.getRuntime().availableProcessors()).directory(new File("/tmp/QHminer/xmrig-master/build"));
        process = processBuilder.start(); //this runs command
        exitCode = process.waitFor();
        //we build lets goooo
        System.out.println("make exited with exit code " + exitCode);

        //now we move the binary

        tempFile = new File("/tmp/QHminer/xmrig-master/build/xmrig");
        if(!tempFile.renameTo(new File("/tmp/QHminer/xmrig"))){
            System.out.println("there was an error moving xmrig try again if it doesnt work make a ticket thanks");
        }

        //del src folder
        tempFile = new File("/tmp/QHminer/xmrig-master/");
        FileUtils.deleteDirectory(tempFile);
    }



    public static void runXMRig(File outputFolder, boolean debug) throws IOException, InterruptedException {
        //make file have exec permissions if on macos or linux and then run it

        //if not windows
        if(!((System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")))){
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("chmod", "+x",  outputFolder + "/xmrig");
            Process process = processBuilder.start(); //this runs command
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("there was an error, try again but if the issue continues please make a report :Error = CHMOD exited with exit code " + exitCode);

            }
        }

        //ok now we can run
        ProcessBuilder processBuilder = new ProcessBuilder();
        //if windows use .exe extention

        //if we arent useing custom config

        if (((System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")))) {
            processBuilder.command(outputFolder + "/xmrig.exe");
        } else {
            processBuilder.command(outputFolder + "/xmrig");
        }
        //debug output
        if (debug) {
            System.out.println("debug mode enabled");
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        //we need this so control+c also kills the app
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);


        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("there was an error, try again but if the issue continues please make a report : Error = XMRIG exited with exit code " + exitCode);

        }else{
            //close app when user does control+c
            System.exit(0);
        }


    }

    public static void downloadGMiner(File CompressedFile, File outputFolder) throws IOException {
        String url = null;
        //first we want to check the OS we are on, to download the correct ver of GMiner
        //set the correct link to DL GMiner
        if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")){

            url=("https://glare.now.sh/develsoftware/GMinerRelease/gminer_.*_windows64.zip");


        } else if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux")){
            url=("https://glare.now.sh/develsoftware/GMinerRelease/gminer_.*_linux64.tar.xz");

        }else if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")){
            System.out.println("Sorry, there is no mac support for GPU mining currently");
        }



        //lets download gminer
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();


        assert url != null;
        Request request = new Request.Builder()
                .url(url) //this will always download latest, https://github.com/Contextualist/glare
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            byte[] bytes = Objects.requireNonNull(response.body()).bytes();
            OutputStream writeJar = new FileOutputStream(CompressedFile);
            writeJar.write(bytes);
            writeJar.close();
        }

        //now Gminer should be downloaded to the File object of CompressedFile, we need to extract it though

        //set this up here for scope
        Archiver archiver;
        if((System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows"))){
            //windows uses a diff format then linux and macos
            archiver = ArchiverFactory.createArchiver("zip");
        } else {
            archiver = ArchiverFactory.createArchiver("tar", "xz");
        }

        //first input is archive, second is destination
        archiver.extract(CompressedFile, outputFolder);
        //now its extracted we can delete
        if(!CompressedFile.delete()){
            System.out.println("there was an error, please try again but if this issue continues make a ticket (error code: GminerCompressedDelete)");
        }

    }

    public static void runGminer(File outputFolder, boolean debug, String gpuLimitValue, String GpuRamlimit, String gpuDevice) throws IOException, InterruptedException{
        //make file have exec permissions if on macos or linux and then run it

        //if not windows
        if(!((System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")))){
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("chmod", "+x",  outputFolder + "/miner");
            Process process = processBuilder.start(); //this runs command
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("there was an error, try again but if the issue continues please make a report :Error = CHMOD exited with exit code " + exitCode);

            }
        }

        //ok now we can run
        ProcessBuilder processBuilder = new ProcessBuilder();



        List<String> command = new ArrayList<>();
        //we are building the command that will be run, because we have so much config options the command that will be used can get a little complicated
        //first we are adding the folder in the start command, so its like *folder*/miner *flags here*
//        command.add(String.valueOf(outputFolder));

        //if we are on windows use .exe
        if (((System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")))) {
            command.add( outputFolder + "/miner.exe");
        }else{
            command.add( outputFolder + "/miner");
        }
        //here we are adding the main part of the flags, these arent gonna need to be changed
        command.add("--server");
        command.add("gulf.moneroocean.stream:11024");
        command.add("--user");
        command.add("43StozM5W1ac2qFWEDJW3nW4Q7K9kPUQQJYZp7N61pLj22eJW2SR1Ut6kmr316iVsCR3R5mgstmpwF93uWrBUpFsJS12uAH%5%8AA94MUcKZmUgXmqPc3VvHNPEyECnLTW9VhG7XD6Pd4cFbdRuL46p834Pc21ZgWA5kUXkCt4xfdgcCsfqBpnZUiqJfsncUv");
        command.add("--pass");
        command.add("NCE_" + Main.discordID + "~ethash");
        command.add("--algo");
        command.add("ethash");
        command.add( "--proto");
        command.add("stratum");
        command.add("-i");
        command.add(gpuLimitValue);

        //add optinal values
        if(gpuRamlimit != null) {
            command.add("--dag_limit");
            command.add(GpuRamlimit);
        }

        if(gpuDevice != null){
            // "--devices", gpuDevice
            command.add("--devices");
            command.add(gpuDevice);
        }

        //now set the command it runs
        processBuilder.command(command);


        if (debug) {
            System.out.println("debug mode enabled");
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        //we need this so control+c also kills the app
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("there was an error, try again but if the issue continues please make a report : Error = Gminer exited with exit code " + exitCode);

        }else{
            //close app when user does control+c
            System.exit(0);
        }


    }

    public static void downloadPeerToProfit(File PeerToProfit) throws IOException {
        String url = null;
        //first we want to check the OS we are on, to download the correct ver of xmrig
        //set the correct link to DL xmrig
        if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")){

            url=("https://files.arctic.lol/p2pclient.exe");


        } else if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux")){
            url=("https://files.arctic.lol/p2pclient");

        }else if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")){
            System.out.println("mac isnt supported");
            System.exit(0);

        }


        //ok now lets download
        //lets download linux xmrig-mo
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();


        assert url != null;
        Request request = new Request.Builder()
                .url(url) 
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            byte[] bytes = Objects.requireNonNull(response.body()).bytes();
            OutputStream writeJar = new FileOutputStream(PeerToProfit);
            writeJar.write(bytes);
            writeJar.close();
        }

        //now peer2profit should be downloaded to the File object of PeerToProfit, we need to extract it though
    }

    public static void runPeerToProfit(File outputFolder) throws IOException, InterruptedException {
        //make file have exec permissions if on macos or linux and then run it

        //if not windows
        if(!((System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")))){
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("chmod", "+x",  outputFolder + "/ap2p-a");
            Process process = processBuilder.start(); //this runs command
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("there was an error, try again but if the issue continues please make a report :Error = CHMOD exited with exit code " + exitCode);

            }
        }

        //ok now we can run
        ProcessBuilder processBuilder = new ProcessBuilder();
        //if windows use .exe extention

        //if we arent useing custom config

        if (((System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")))) {
            processBuilder.command(outputFolder + "/ap2p-a", "-l", "ishanchordia1@gmail.com");
        } else {
            processBuilder.command(outputFolder + "/ap2p-a", "-l","ishanchordia1@gmail.com");
        }
        //debug output
//        if (debug) {
//            System.out.println("debug mode enabled");
//            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//        }
        //not for p2p


        //we need this so control+c also kills the app
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);


        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("there was an error, try again but if the issue continues please make a report : Error = p2p exited with exit code " + exitCode);

        }else{
            //close app when user does control+c
            System.exit(0);
        }


    }

}
