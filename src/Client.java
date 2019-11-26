import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Base64;
import java.util.Scanner;

import org.json.simple.*;
import org.json.simple.parser.*;


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
	
	public JSONObject sendData(String data) {
		try {
			DatagramSocket socket = new DatagramSocket();
			InetAddress IP = InetAddress.getByName(this.host);
			JSONObject request = new JSONObject();
			String secretKey = "this_is_secret_key";
			System.out.println(secretKey);
			data = AES.encrypt(data, secretKey);
            String secretKeyEncrypted = RSA.encrypt(secretKey);
            request.put("data", data);
            request.put("public_key", Base64.getEncoder().encodeToString(RSA_Key.publicKey.getEncoded()));
            request.put("secret_key", secretKeyEncrypted);
            this.outData = request.toString().getBytes();
          //gửi dữ liệu tới server udp
            DatagramPacket sendPkt = new DatagramPacket(this.outData, this.outData.length, IP, this.port);
            socket.send(sendPkt);
            socket.setSoTimeout(this.timeout);
          //chờ nhận dữ liệu từ udp server gửi về
            DatagramPacket recievePkt = new DatagramPacket(this.inData, this.inData.length);
            socket.receive(recievePkt);
            String strReceived = new String(recievePkt.getData(), 0, recievePkt.getLength());
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(strReceived);
            String res = AES.decrypt(response.get("data").toString(), secretKey);
            System.out.println(res);
			JSONObject result = (JSONObject) parser.parse(AES.decrypt(response.get("data").toString(), secretKey));
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
		while(true) {
			System.out.print("Nhập mã số sinh viên:");
			String out = keybroad.nextLine();
			Client cli = new Client();
			JSONObject data = cli.sendData(out);
			System.out.println("=========");
			System.out.println("Họ và tên:\t" + data.get("hoTen"));
			System.out.println("Mã số sv:\t" + data.get("mssv"));
			System.out.println("Phái:\t" + data.get("phai"));
			System.out.println("Ngày sinh:\t" + data.get("ngaySinh"));
			System.out.println("Nơi sinh:\t" + data.get("noiSinh"));
			System.out.println("Lớp:\t" + data.get("lop"));
			System.out.println("Ngành:\t" + data.get("nganh"));
			System.out.println("Khoa:\t" + data.get("khoa"));
			System.out.println("Chuyên ngành:\t" + data.get("chuyenNganh"));
			System.out.println("Hệ đào tạo:\t" + data.get("heDaoTao"));
			System.out.println("Khóa học:\t" + data.get("khoaHoc"));
			System.out.println("Cố vấn:\t" + data.get("coVan"));
			System.out.println("=========");
			System.out.println("Điểm trung bình hệ 10:\t" + data.get("tb10"));
			System.out.println("Điểm trung bình hệ 4:\t" + data.get("tb4"));
			System.out.println("Số tính chỉ:\t" + data.get("tc"));
			System.out.println("Số lượng môn học:\t" + data.get("sl"));
			System.out.println("=========================================");
		}
	}
}
