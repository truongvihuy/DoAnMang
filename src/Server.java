import java.util.List;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

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
		// Khởi chạy UDP server với port 8000;
		socket = new DatagramSocket(8000);
		System.out.println("Server is running!!!");
		// Tạo packet nhận dữ liệu
		receivePacket = new DatagramPacket(inp, inp.length);
	}
	public static JSONObject getData(String mssv) throws IOException {
		try (final WebClient webClient = new WebClient()) {
			// Get the first page
	        HtmlPage page1 = webClient.getPage("http://thongtindaotao.sgu.edu.vn/Default.aspx?page=xemdiemthi");

	        // Get the form that we are dealing with and within that form, 
	        // find the submit button and the field that we want to change.
	        HtmlForm form = page1.getFormByName("aspnetForm");

	        HtmlSubmitInput button = form.getInputByName("ctl00$ContentPlaceHolder1$ctl00$btnOK");
	        HtmlTextInput textField = form.getInputByName("ctl00$ContentPlaceHolder1$ctl00$txtMaSV");

	        // Change the value of the text field
	        textField.type(mssv);

	        // Now submit the form by clicking the button and get back the second page.
	        final HtmlPage page2 = button.click();
	        
	        HtmlAnchor htmlAnchor = page2.getAnchorByHref("javascript:__doPostBack('ctl00$ContentPlaceHolder1$ctl00$lnkChangeview2','')");
	        HtmlPage page3 = htmlAnchor.click();
	        
	        JSONObject data = new JSONObject();
	        // Get info student
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
	        
	        // 
	        DomNodeList<HtmlElement> diem = page3.getElementById("ctl00_ContentPlaceHolder1_ctl00_div1").getElementsByTagName("tr");
	        String tb10 = "";
	        String tb4 = "";
	        String tc = "";
	        List<String> dsmh = new ArrayList<String>();
	        for (HtmlElement row : diem) {
	        	switch(row.getAttribute("class")){
	        	case "title-diem": break;
	        	case "title-hk-diem": break;
	        	case "row-diem": 
	        		String maMonHoc = row.getElementsByTagName("td").get(1).asText();
	        		if(maMonHoc.equals("KSTA60")) break; 
	        		boolean flag = true;
	        		for(String mh : dsmh) {
	        			if(mh.equals(maMonHoc)) {
	        				flag = false;
	        				break;
	        			}
	        		}
	        		if(flag) dsmh.add(maMonHoc);
	        		break;
	        	case "row-diemTK": 
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
	        data.put("sl", Integer.toString(dsmh.size()));
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
	            
				JSONObject result = getData(data);
				System.out.println("Packet send: " + result);
				out = result.toString().getBytes();
				DatagramPacket sendPacket = new DatagramPacket(out, out.length, ip, port);
	            socket.send(sendPacket);
	            System.out.println("=========================");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
