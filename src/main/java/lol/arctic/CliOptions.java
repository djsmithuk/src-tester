package lol.arctic;

import org.apache.commons.cli.*;


public class CliOptions {
    public Options cliOptions(){
      Options options = new Options();
        options.addOption("interactive","interactive",false,"so the miner will ask you things instead of setting via flags");
        options.addOption("cpulimit", "cpulimit",true,"Limit the amount of cpu used");

        options.addOption("id","id",true,"specifiy ID before running miner");
        options.addOption("gpu","gpu",false,"Use GPU to mine");
        options.addOption("gpulimit","gpulimit",true,"Limit the amount of gpu used");
        options.addOption("hybrid","hybrid",false,"Use both the GPU and the CPU to mine");
        options.addOption("childcomputer","childcomputer",false,"Use this if you want to mine on multiple computers, one (not more) computer must be running without this option");
        options.addOption("gpuRamLimit","gpuRamLimit",true,"Use this if you get \"Out of memory\" error, set it to the vram your card has (if issue persists maybe a bit lower), size is in megabytes");
        options.addOption("gpuSelectDevice","gpuSelectDevice",true,"Use this if you want to select a device to use for gpu mining (such as gpu 1 or gpu 2)");
        options.addOption("compileXmrig","compileXmrig",false,"Use this to manually compile (currently only supports arch linux), good for phones or non-x86_64 devices");

        options.addOption("help","help",false,"help menu");

        return options;
    }

    public int parseOptionsCpulimit(Options options, String[] args) {
        int value = 100;
        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options,args);
            if (line.hasOption("cpulimit")) {
                try{
                    value =  Integer.parseInt(line.getOptionValue("cpulimit"));
                }catch (Exception MissingArgumentException){
                    System.out.println("You need to pass a number for cpulimit, example --cpulimit 30");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }



        return value;
    }

    public boolean parseOptionsInteractive(Options options, String[] args) throws ParseException {

        CommandLineParser parser = new DefaultParser();


        // parse the command line arguments
        CommandLine line = parser.parse(options,args);
        return line.hasOption("interactive");

    }

//    public boolean parseOptionsDontRoundHashrate(Options options, String[] args) throws ParseException {
//
//        CommandLineParser parser = new DefaultParser();
//
//
//        // parse the command line arguments
//        CommandLine line = parser.parse(options,args);
//        return line.hasOption("DontRoundHashrate");
//    }
//
//
//
//    public boolean parseOptionsNoDebug(Options options, String[] args) throws ParseException {
//
//        CommandLineParser parser = new DefaultParser();
//
//
//        // parse the command line arguments
//        CommandLine line = parser.parse(options,args);
//        return !line.hasOption("noDebug");
//
//    }



    public boolean parseOptionsCompileXmrig(Options options, String[] args) throws ParseException {

        CommandLineParser parser = new DefaultParser();


        // parse the command line arguments
        CommandLine line = parser.parse(options,args);
        return line.hasOption("compileXmrig");
    }


    public void parseOptionsHelp(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options,args);
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar miner.jar", options);
            System.exit(0);
        }

    }


    public String parseOptionsID(Options options, String[] args) {
        String value = "0";
        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options,args);
            if (line.hasOption("id")) {
                try{
                    value = line.getOptionValue("id");
                }catch (Exception MissingArgumentException){
                    System.out.println("you need to put your ID if you use --id flag, like this java -jar miner.jar --id 316818056049590282");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }


        //value will be 0 if they dont use ID option
        return value;
    }


    public boolean parseOptionsGpu(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options,args);
        return line.hasOption("gpu");
    }

    public boolean parseOptionsHybrid(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options,args);
        return line.hasOption("hybrid");
    }

    public boolean parseOptionsChildComputer(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options,args);
        return line.hasOption("childcomputer");
    }

    public String parseOptionsGpulimit(Options options, String[] args) {
        String value = "100";
        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options,args);
            if (line.hasOption("gpulimit")) {
                try{
                    value =  line.getOptionValue("gpulimit");
                }catch (Exception MissingArgumentException){
                    System.out.println("You need to pass a number for gpulimit, example --gpuimit 30");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }



        return value;
    }

    public String parseOptionsGpuRamlimit(Options options, String[] args) {
        String value = null;
        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options,args);
            if (line.hasOption("GpuRamlimit")) {
                try{
                    value =  line.getOptionValue("GpuRamlimit");
                }catch (Exception MissingArgumentException){
                    System.out.println("You need to pass a number for gpulimit, example --GpuRamlimit 4096");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }



        return value;
    }

    public String parseOptionsGpuSelectDevice(Options options, String[] args) {
        String value = null;
        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options,args);
            if (line.hasOption("gpuSelectDevice")) {
                try{
                    value =  line.getOptionValue("gpuSelectDevice");
                }catch (Exception MissingArgumentException){
                    System.out.println("You need to pass a number for gpuSelectDevice, example --gpuSelectDevice 4");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }



        return value;
    }


    //    public void parseOptionsdebug(Options options, String[] args) throws ParseException {
//
//        CommandLineParser parser = new DefaultParser();
//
//
//            // parse the command line arguments
//            CommandLine line = parser.parse(options,args);
//        if(line.hasOption("debug")){
//            System.out.println("The debug flag is now deprecated, debug mode is default, please dont use this flag");
//            System.out.println("the new flag is --noDebug");
//            System.out.println("In the future this flag (--debug) will be completely removed");
//        }
//
//    }


}
