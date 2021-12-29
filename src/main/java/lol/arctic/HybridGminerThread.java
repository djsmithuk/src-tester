package lol.arctic;

public class HybridGminerThread implements Runnable{



    @Override
    public void run()  {
        try {
            Main.runGminer(Main.outputFolder, Main.debug, Main.gpuLimitValue, Main.gpuRamlimit, Main.gpuDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
