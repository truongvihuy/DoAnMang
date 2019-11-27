package Client;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	
	public JSONObject sendMSSV(String mssv) throws SocketTimeoutException, Exception {
		return this.sendData(mssv);
	}
	
	public JSONObject sendData(String data) throws SocketTimeoutException, Exception{
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
            
            
		// Chờ nhận dữ liệu từ Server
		DatagramPacket recievePkt = new DatagramPacket(this.inData, this.inData.length);
		socket.receive(recievePkt);
		String strReceived = new String(recievePkt.getData(), 0, recievePkt.getLength());
            
		// Tiến hành mã hóa
		String res = AES.decrypt(strReceived, secretKey);
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(res);
		if(response.get("success").toString().equals("true")) {
			JSONObject result = (JSONObject) parser.parse(response.get("data").toString());
			System.out.println(result);
			return result;			
		} else {
			throw new Exception(response.get("error").toString());
		}
	}
	
	public static void main (String[] args) {
		Scanner keybroad = new Scanner(System.in); 
		RSA_Key.GenerateKeys();
		while(true) {
			System.out.print("Nhập mssv:");
			String out = keybroad.nextLine();
			try {
				Client cli = new Client();
				JSONObject data = cli.sendMSSV(out);
				System.out.println("===================================================");
				System.out.println("Họ và tên:\t" + data.get("hoTen"));
				System.out.println("Mã sv:\t\t" + data.get("mssv"));
				System.out.println("Phái:\t\t" + data.get("phai"));
				System.out.println("Ngày sinh:\t" + data.get("ngaySinh"));
				System.out.println("Nơi sinh:\t" + data.get("noiSinh"));
				System.out.println("Lớp:\t\t" + data.get("lop"));
				System.out.println("Ngành:\t\t" + data.get("nganh"));
				System.out.println("Khoa:\t\t" + data.get("khoa"));
				System.out.println("Chuyên ngành:\t" + data.get("chuyenNganh"));
				System.out.println("Hệ đào tạo:\t" + data.get("heDaoTao"));
				System.out.println("Khóa hợc:\t" + data.get("khoaHoc"));
				System.out.println("Cố vấn:\t\t" + data.get("coVan"));
				System.out.println("\t"+"============================");
				JSONArray danhSachHocKi = (JSONArray) data.get("danhSachHocKi");
				for (int i = 0; i < danhSachHocKi.size(); i++) {
					JSONObject hocKi = (JSONObject) danhSachHocKi.get(i);
					System.out.println("\t" + hocKi.get("hocKi"));
					JSONArray danhSachMonHoc = (JSONArray) hocKi.get("monHoc");
					for(int j = 0; j < danhSachMonHoc.size(); j++) {
						JSONObject monHoc = (JSONObject) danhSachMonHoc.get(j);
						System.out.println(monHoc.get("tenMon"));
						System.out.println("\tKiểm tra: " + monHoc.get("kiemTra") + "\t\tThi: "  + (monHoc.get("thi").toString().length() > 1 ? monHoc.get("thi") : "   ")+ "\tTổng kết(10): " + monHoc.get("tongKet10") + "\tTổng kết(4): " + monHoc.get("tongKet4") + "\t\tKết quả: " + monHoc.get("ketQua") + "\n");
					}
					System.out.println("\t"+"============================");
				}
				System.out.println("Điểm trung bình tích lũy hệ 10:\t" + data.get("tb10"));
				System.out.println("Điểm trung bình tích lũy hệ 4:\t" + data.get("tb4"));
				System.out.println("Tính chỉ tích lũy:\t\t" + data.get("tc"));
				System.out.println("Số lượng môn học:\t\t" + data.get("sl"));
				System.out.println("===================================================");
			} catch (SocketTimeoutException e) {
				System.out.println("Dữ liệu phản hồi khá lâu!!!");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
