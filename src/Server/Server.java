package Server;

import java.util.List;
import java.util.regex.Pattern;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import RSA.AES;
import RSA.RSA;

// title-diem
// title-hk-diem
// row-diem

public class Server {
	static String mssv = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblMaSinhVien";
	static String hoten = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblTenSinhVien";
	static String phai = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblPhai";
	static String ngaysinh = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblNgaySinh";
	static String noisinh = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblNoiSinh";
	static String lop = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblLop";
	static String nganh = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lbNganh";
	static String chuyennganh = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblChNg";
	static String khoa = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblKhoa";
	static String hedaotao = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblHeDaoTao";
	static String khoahoc = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblKhoaHoc";
	static String covan = "ctl00_ContentPlaceHolder1_ctl00_ucThongTinSV_lblCVHT";
	static String chitietdiem = "ctl00_ContentPlaceHolder1_ctl00_div1";
	
	private static DatagramSocket socket = null;
	private static DatagramPacket receivePacket = null;
	private static byte[] inp = new byte[102400];
	private static byte[] out = new byte[102400];
	public Server() throws IOException {
		// Khởi tạo server với port 8000
		socket = new DatagramSocket(8000);
		System.out.println("Server is running!!!");
		// Tạo packet nhận dữ liệu
		receivePacket = new DatagramPacket(inp, inp.length);
	}
	public static JSONObject getData(String mssv) {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
		try (final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
			Pattern patt = Pattern.compile("^[123]1\\d{8}$");
			if(!patt.matcher(mssv).matches()) throw new Exception("Mã sinh viên không hợp lệ!!!");
			// Vào trang xem điểm thi
	        HtmlPage page1 = webClient.getPage("http://thongtindaotao.sgu.edu.vn/default.aspx?page=nhapmasv&flag=XemDiemThi");
	        
	        // Tìm đến Form có tên aspnetFrom và tìm đến textInput, btn
	        HtmlForm form = page1.getFormByName("aspnetForm");
	        HtmlSubmitInput button = form.getInputByName("ctl00$ContentPlaceHolder1$ctl00$btnOK");
	        HtmlTextInput textField = form.getInputByName("ctl00$ContentPlaceHolder1$ctl00$txtMaSV");
	        System.out.println(button.asText());
	        // Điền mssv vào text Input
	        textField.type(mssv);

	        // Chuyển đến trang thứ 2
	        final HtmlPage page2 = button.click();
	        
	        // Tìm đến nút xem tất và tiến hành click để chuyển page 
	        HtmlAnchor htmlAnchor = page2.getAnchorByHref("javascript:__doPostBack('ctl00$ContentPlaceHolder1$ctl00$lnkChangeview2','')");
	        HtmlPage page3 = htmlAnchor.click();
	        
	        JSONObject data = new JSONObject();
	        // Lấy thông tin sinh viên
	        data.put("mssv", mssv);
	        data.put("hoTen", page3.getElementById(hoten).asText());
	        data.put("phai", page3.getElementById(phai).asText());
	        data.put("ngaySinh", page3.getElementById(ngaysinh).asText());
	        data.put("noiSinh", page3.getElementById(noisinh).asText());
	        data.put("lop", page3.getElementById(lop).asText());
	        data.put("nganh", page3.getElementById(nganh).asText());
	        data.put("chuyenNganh", page3.getElementById(chuyennganh).asText());
	        data.put("khoa", page3.getElementById(khoa).asText());
	        data.put("heDaoTao", page3.getElementById(hedaotao).asText());
	        data.put("khoaHoc", page3.getElementById(khoahoc).asText());
	        data.put("coVan", page3.getElementById(covan).asText());
	        
	        // Lấy phần khung bảng điểm
	        int demMonhocMoiTrongHocKi = 0;
	        DomNodeList<HtmlElement> diem = page3.getElementById("ctl00_ContentPlaceHolder1_ctl00_div1").getElementsByTagName("tr");
	        String tb10 = "";
	        String tb4 = "";
	        String tc = "";
	        List<String> dsmh = new ArrayList<String>();
	        JSONArray danhSachHocKi = new JSONArray();
	        JSONObject hocki = null;
	        JSONArray monHocHocKi = null;
	        for (HtmlElement row : diem) {
	        	switch(row.getAttribute("class")){
	        		case "title-diem": break;
		        	case "title-hk-diem": 
		        		if(hocki != null) {
		        			hocki.put("monHoc", monHocHocKi);
		        			danhSachHocKi.add(hocki);
		        			hocki = null;
		        			monHocHocKi = null;
		        		}
		        		demMonhocMoiTrongHocKi = 0;
		        		hocki = new JSONObject();
		        		monHocHocKi = new JSONArray();
		        		hocki.put("hocKi", row.asText());
		        		break;
		        	case "row-diem": 
		        		DomNodeList<HtmlElement> monHoc = row.getElementsByTagName("td");
		        		if(monHoc.get(1).asText().equals("KSTA60")) break; 
		        		JSONObject tmpMonHoc = new JSONObject();
		        		tmpMonHoc.put("maMon", monHoc.get(1).asText());
		        		tmpMonHoc.put("tenMon", monHoc.get(2).asText());
		        		tmpMonHoc.put("kiemTra", monHoc.get(6).asText());
		        		tmpMonHoc.put("thi", monHoc.get(7).asText());
		        		tmpMonHoc.put("tongKet10", monHoc.get(8).asText());
		        		tmpMonHoc.put("tongKet4", monHoc.get(9).asText());
		        		tmpMonHoc.put("ketQua", monHoc.get(10).asText());
		        		monHocHocKi.add(tmpMonHoc);
		        		// Đếm môn học
		        		boolean flag = true;
		        		for(String mh : dsmh) {
		        			if(mh.equals(monHoc.get(1).asText())) {
		        				flag = false;
		        				break;
		        			}
		        		}
		        		if(flag) {
		        			dsmh.add(monHoc.get(1).asText());
		        			demMonhocMoiTrongHocKi++;
		        		}
		        		break;
		        	case "row-diemTK": 
		        		if(hocki != null) {
		        			hocki.put("monHoc", monHocHocKi);
		        			danhSachHocKi.add(hocki);
		        			hocki = null;
		        			monHocHocKi = null;
		        		}
		        		DomNodeList<HtmlElement> tmp = row.getElementsByTagName("span");
		        		switch (row.getElementsByTagName("span").get(0).asText()){
		        			case "Điểm trung bình tích lũy:": tb10 = tmp.get(1).asText(); break;
		        			case "Điểm trung bình tích lũy (hệ 4):": tb4 = tmp.get(1).asText(); break;
		        			case "Số tín chỉ tích lũy:": tc = tmp.get(1).asText(); break;
		        		}
		        		break;
	        	}
	        }
	        data.put("tb10", tb10);
	        data.put("tb4", tb4);
	        data.put("tc", tc);
	        data.put("sl", Integer.toString(dsmh.size() - demMonhocMoiTrongHocKi));
	        data.put("danhSachHocKi", danhSachHocKi);
	        
	        JSONObject response = new JSONObject();
	        response.put("data", data.toString());
	        response.put("success", true);
	        return response;
		} catch (ElementNotFoundException e) {
			JSONObject data = new JSONObject();
			data.put("success", false);
			data.put("error", "Không tìm thấy mã vừa nhập!!!");
			return data;
		}catch (NullPointerException e) {
			JSONObject data = new JSONObject();
			data.put("success", false);
			data.put("error", "Không tìm thấy thông tin!!!");
			return data;
		} catch (Exception e) {
			JSONObject data = new JSONObject();
			System.out.println(e);
			data.put("success", false);
			data.put("error", e.getMessage());
			return data;
		}
	}
	public static void main(String[] args) {
		try {
			new Server();
			while(true) {
				socket.receive(receivePacket);
				System.out.println("=== Server received a packet!!! ===");
				InetAddress ip = receivePacket.getAddress();
				System.out.println("IP Address: " + ip);
				int port = receivePacket.getPort();
				System.out.println("Port:" + port);
				inp = receivePacket.getData();
				String data = new String(inp, 0, receivePacket.getLength());
				System.out.println("Packet received: " + data);
				
				JSONParser parser = new JSONParser();
				JSONObject request = (JSONObject) parser.parse(data);
				String key = request.get("secret_key").toString();
				key = RSA.decrypt(key);
	            data = AES.decrypt(request.get("data").toString(), key);
	            
	            String result;
	            // Tiến hành lấy dữ liệu từ trang web và trả về kết quả
	            result = getData(data).toString();
	            // Mã hóa dữ liệu
	            result = AES.encrypt(result, key);
	            // Tiến hành chuyển dữ liệu sang kiểu byte và chuyển về lại client
	            out = result.getBytes();
	            DatagramPacket sendPacket = new DatagramPacket(out, out.length, ip, port);
	            socket.send(sendPacket);
	            System.out.println("=========================");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
