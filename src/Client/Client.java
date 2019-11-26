package Client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Base64;
import java.util.Scanner;

import org.json.simple.*;
import org.json.simple.parser.*;

import RSA.AES;
import RSA.RSA;
import RSA.RSA_Key;


public class Client {
	private byte[] inData = new byte[102400];
    private byte[] outData = new byte[102400];
    private int timeout;
    private String host;
    private int port;
    
	public Client(int port,int timeout){
		this.host = "localhost";
		this.port = port;
		this.timeout = timeout;
	}
	
	public Client(){
		this.host = "localhost";
		this.port = 8000; 
		this.timeout = 10000;
	}
	
	public JSONObject sendMSSV(String mssv) {
		return this.sendData(mssv);
	}
	
	public JSONObject sendData(String data) {
		try {
			// Tiến hành mã hóa dữ liệu
			String secretKey = "this_is_secret_key";
			data = AES.encrypt(data, secretKey);
            String secretKeyEncrypted = RSA.encrypt(secretKey); 
            // Tạo data request kèm theo public_key và secret_key
            JSONObject request = new JSONObject();
            request.put("data", data);
            request.put("public_key", Base64.getEncoder().encodeToString(RSA_Key.publicKey.getEncoded()));
            request.put("secret_key", secretKeyEncrypted);
            System.out.println(request);
            this.outData = request.toString().getBytes();
            
            // Gửi UDP đến Server
            DatagramSocket socket = new DatagramSocket();
			InetAddress IP = InetAddress.getByName(this.host);
            DatagramPacket sendPkt = new DatagramPacket(this.outData, this.outData.length, IP, this.port);
            socket.send(sendPkt);
            socket.setSoTimeout(this.timeout);
            
            
            // Chờ nhận dữ liệu từ server
            DatagramPacket recievePkt = new DatagramPacket(this.inData, this.inData.length);
            socket.receive(recievePkt);
            String strReceived = new String(recievePkt.getData(), 0, recievePkt.getLength());
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(strReceived);
            System.out.println(response);
            
            String res = AES.decrypt(response.get("data").toString(), secretKey);
            System.out.println(res);
			JSONObject result = (JSONObject) parser.parse(AES.decrypt(response.get("data").toString(), secretKey));
			System.out.println(result);
            return result;
		} catch (SocketTimeoutException e) {
			System.out.println("Dữ liệu phản hồi quá lâu!!!");
			JSONObject result = new JSONObject();
			result.put("success", false);
			return result;
		} catch (Exception e) {
			System.out.println(e);
			JSONObject result = new JSONObject();
			result.put("success", false);
			return result;
		}
		
	}
	
	public static void main (String[] args) {
		Scanner keybroad = new Scanner(System.in); 
		RSA_Key.GenerateKeys();
		while(true) {
			System.out.print("Nhập mssv:");
			String out = keybroad.nextLine();
			Client cli = new Client();
			JSONObject data = cli.sendMSSV(out);
			if(data.get("success").toString().equals("true")) {
	//			System.out.println("=========");
	//			System.out.println("Há»� vÃ  tÃªn:\t" + data.get("hoTen"));
	//			System.out.println("MÃ£ sá»‘ sv:\t" + data.get("mssv"));
	//			System.out.println("PhÃ¡i:\t" + data.get("phai"));
	//			System.out.println("NgÃ y sinh:\t" + data.get("ngaySinh"));
	//			System.out.println("NÆ¡i sinh:\t" + data.get("noiSinh"));
	//			System.out.println("Lá»›p:\t" + data.get("lop"));
	//			System.out.println("NgÃ nh:\t" + data.get("nganh"));
	//			System.out.println("Khoa:\t" + data.get("khoa"));
	//			System.out.println("ChuyÃªn ngÃ nh:\t" + data.get("chuyenNganh"));
	//			System.out.println("Há»‡ Ä‘Ã o táº¡o:\t" + data.get("heDaoTao"));
	//			System.out.println("KhÃ³a há»�c:\t" + data.get("khoaHoc"));
	//			System.out.println("Cá»‘ váº¥n:\t" + data.get("coVan"));
	//			System.out.println("=========");
	//			System.out.println("Ä�iá»ƒm trung bÃ¬nh há»‡ 10:\t" + data.get("tb10"));
	//			System.out.println("Ä�iá»ƒm trung bÃ¬nh há»‡ 4:\t" + data.get("tb4"));
	//			System.out.println("Sá»‘ tÃ­nh chá»‰:\t" + data.get("tc"));
	//			System.out.println("Sá»‘ lÆ°á»£ng mÃ´n há»�c:\t" + data.get("sl"));
	//			System.out.println("=========================================");
			} else {
				//
			}
		}
	}
}
