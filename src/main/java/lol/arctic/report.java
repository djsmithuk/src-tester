package lol.arctic;




import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class report implements Runnable{

    @Override
    public void run() {
        try {
            main(null);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        //cool we do stuff here

        //make so we dont need to recreate onject each time
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();


        while (true){

            try {

                setCoinAmount(okHttpClient);

                //Get current hashrate, to see earnings
//                hashRate = getHashrate(okHttpClient);

            }catch (SocketTimeoutException e){
                System.out.println("connection to seanodes timed out, likely an issue on their end, wait a few min and see if you dont get this issue again");
                //this is ok we retry next min
            }catch (Exception e){
                e.printStackTrace();
            }


            TimeUnit.MINUTES.sleep(1);
        }



    }

    public static void setCoinAmount(OkHttpClient okHttpClient) throws IOException {


        Request addCoinsRequest = new Request.Builder()
                .url("https://seanodes.xyz/api/check_miner&add_coins/discord_id=" + Main.discordID)
                .build();

        Response addCoinsResponce = okHttpClient.newCall(addCoinsRequest).execute();

        JSONObject jsonObject = new JSONObject();

        try {
             jsonObject = new JSONObject(Objects.requireNonNull(addCoinsResponce.body()).string());
        }catch (Exception e){
            if(e.toString().contains("<title>429 Too Many Requests</title>")){
                System.out.println("rate limit error, you shouldnt see this, hopefully this works in the next minute");
            }else{
                e.printStackTrace();
            }


        }



        if(jsonObject.get("status").toString().equals("200")){
            System.out.println("coins received: " + roundOffTo2DecPlaces(jsonObject.get("coins_received").toString()));
            System.out.println("Your current coins: " + roundOffTo2DecPlaces(jsonObject.get("end_coins").toString()));
            System.out.println("current hashrate: " + roundOffTo2DecPlaces(jsonObject.get("current_hashrate").toString()));
        }else if(jsonObject.get("message").toString().equals("No miner running")){
            System.out.println("the miner is still warming up, please wait, this may take up to 30 min");
        }else {
            System.out.println("there was an error please make a ticket and ping Arctic");
            System.out.println("Api responce = ");
            System.out.println(addCoinsRequest.body().toString());
        }


        addCoinsResponce.close();




    }


  public static  String roundOffTo2DecPlaces(String val) {
        //https://stackoverflow.com/questions/11701399/round-up-to-2-decimal-places-in-java


        return String.format("%.2f", Float.parseFloat(val));
    }

//    public static String getHashrate(OkHttpClient okHttpClient){
//
//        String hashRate;
//        try {
//
////QH MINER
//
//            Request requestHashrate = new Request.Builder()
//                    .url("https://api.moneroocean.stream/miner/43StozM5W1ac2qFWEDJW3nW4Q7K9kPUQQJYZp7N61pLj22eJW2SR1Ut6kmr316iVsCR3R5mgstmpwF93uWrBUpFsJS12uAH/stats/" + "NCE_" + Main.discordID)
//                    .build();
//
//            Response responseHashrate = okHttpClient.newCall(requestHashrate).execute();
//            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(responseHashrate.body()).string());
//
//                System.out.println("Current Hashrate: " + jsonObject.get("hash2").toString());
//
//
//                hashRate = jsonObject.get("hash2").toString();
//                responseHashrate.close();
//
//            return hashRate;
//        }catch (Exception e){
//            //its ok for this to error before u show up on MO, we might need better way to handle this tho
//            System.out.println("please wait, miner is still warming up, if you see this message after 1h of mining please make a ticket");
//            return "0";
//        }
//
//    }



}
