import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FOTAServer {

    public static void main(String[] args) {
        int port = 10000;
        ServerSocket serverSocket = null;
        Socket socket = null;

        try{
            serverSocket = new ServerSocket(port);
            System.out.println("서버 소켓 실행");
            serverSocket.accept();
            System.out.println("연결완료");

            FileReceiver fl = new FileReceiver(socket);
            fl.start();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class FileReceiver extends Thread{
    Socket socket;
    InputStream inputStream;
    OutputStream outputStream;
    DataOutputStream dataOutputStream;

    String dirPath = "C:\\Users\\lsmn0\\Documents\\LSM\\FOTAImage\\";
//    String dirPath = "/home/pi/fotaserver";

    ArrayList<String> files = new ArrayList<>();

    public FileReceiver(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        boolean isValid = false;
        String data[] = new String[20];

        /*디렉토리 목록 불러오기*/
        /*
        File dirFile=new File(dirPath);
        File []fileList=dirFile.listFiles();
        for(File tempFile : fileList) {
            if(tempFile.isFile()) {
                String tempFileName = tempFile.getName();
                files.add(tempFile.getName());
                System.out.println(tempFileName);
            }
        }
         */
        File dirFile=new File(dirPath);
        File []fileList=dirFile.listFiles();
        File tempFile = fileList[0];
        String fileName = null;
        if(tempFile.isFile()) {
            fileName = tempFile.getName();
            System.out.println(fileName);
        }

        try {
            // Request 받기
            inputStream = socket.getInputStream();
            System.out.println("AAAA");
            byte[] receiveBuffer = new byte[100];
            inputStream.read(receiveBuffer);

            System.out.println(receiveBuffer);

            // Request 파싱
            // Request 포맷 [0x02][DATA][0x0d][0x03]
            if(receiveBuffer[0] == 0x02) {
                System.out.println("Valid Request");
                for(int i = 1; receiveBuffer[i] != 0x03; i++) {
                    data[i] = String.valueOf(receiveBuffer[i]);
                }
                System.out.println("Request Data: " + data);
                isValid = true;
            } else {
                System.out.println("Invalid Request");
                isValid = false;
            }

            // Response
            // Response Valid: [0x02][DATA file name][0x0d][0x03] Invalid: 0x15
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);

            if (isValid) {
                dataOutputStream.write(0x02);
                for(int i = 0; fileName.charAt(i) != '\0'; i++)
                    dataOutputStream.write(fileName.charAt(i));
                dataOutputStream.write(0x0d);
                dataOutputStream.write(0x03);
            } else {
                // NAK
                dataOutputStream.write(0x15);
            }

            inputStream.close();
            dataOutputStream.close();
            outputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}