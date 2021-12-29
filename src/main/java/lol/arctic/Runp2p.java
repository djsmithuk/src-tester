package lol.arctic;

public class Runp2p  implements Runnable{

        @Override
        public void run()  {
            try {
                Main.runPeerToProfit(Main.outputFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


