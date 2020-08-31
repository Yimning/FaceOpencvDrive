package com.cameratest;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: Yimning
 * @date: 2020/2/29  16:12
 * @description: 开启本地摄像头，进行脸部跟踪，以及图片抓取与保存
 */

public class CapturePhotos extends JPanel {


    private static BufferedImage mImg;

    private BufferedImage mat2BI(Mat mat){
        int dataSize =mat.cols()*mat.rows()*(int)mat.elemSize();
        byte[] data=new byte[dataSize];
        mat.get(0, 0,data);
        int type=mat.channels()==1?
                BufferedImage.TYPE_BYTE_GRAY:BufferedImage.TYPE_3BYTE_BGR;

        if(type==BufferedImage.TYPE_3BYTE_BGR){
            for(int i=0;i<dataSize;i+=3){
                byte blue=data[i+0];
                data[i+0]=data[i+2];
                data[i+2]=blue;
            }
        }
        BufferedImage image=new BufferedImage(mat.cols(),mat.rows(),type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

        return image;
    }

    public void paintComponent(Graphics g){
        if(mImg!=null){
            g.drawImage(mImg, 0, 0, mImg.getWidth(),mImg.getHeight(),this);
        }
    }

    public static void main(String[] args) {
        try{
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            Mat capImg=new Mat();
            VideoCapture capture=new VideoCapture(0);
            int height = (int)capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            int width = (int)capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            if(height==0||width==0){
                throw new Exception("camera not found!");
            }

            JFrame frame=new JFrame("camera");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            CapturePhotos panel=new CapturePhotos();
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    System.out.println("click");
                }
                @Override
                public void mouseMoved(MouseEvent arg0) {
                    System.out.println("move");

                }
                @Override
                public void mouseReleased(MouseEvent arg0) {
                    System.out.println("mouseReleased");
                }
                @Override
                public void mousePressed(MouseEvent arg0) {
                    System.out.println("mousePressed");
                }
                @Override
                public void mouseExited(MouseEvent arg0) {
                    System.out.println("mouseExited");
                    //System.out.println(arg0.toString());
                }
                @Override
                public void mouseDragged(MouseEvent arg0) {
                    System.out.println("mouseDragged");
                    //System.out.println(arg0.toString());
                }
            });
            frame.setContentPane(panel);
            frame.setVisible(true);
            frame.setSize(width+frame.getInsets().left+frame.getInsets().right,
                    height+frame.getInsets().top+frame.getInsets().bottom);
            int n=0;
            Mat temp=new Mat();
            while(frame.isShowing()&& n<500){
                //System.out.println("第"+n+"张");
                capture.read(capImg);
                Imgproc.cvtColor(capImg, temp, Imgproc.COLOR_RGB2GRAY);

               // Imgcodecs.imwrite("C:\\E盘(文件)\\IdeaProjects\\FaceMaskSample\\images"+n+".png", temp);

                panel.mImg=panel.mat2BI(detectFace(capImg));

                //拍照
                SimpleDateFormat data = new SimpleDateFormat("yyyyMMddHHmmss");
                String name = data.format(new Date());
                //获取桌面路径
                //File path = FileSystemView.getFileSystemView().getHomeDirectory();
                String path = "C:\\E盘(文件)\\IdeaProjects\\FaceMaskSample\\images";
                System.out.println(path);
                String format = "jpeg";
                File f = new File(path + File.separator + name + "." + format);
                try {
                    System.out.println(name);
                    ImageIO.write(mImg, format, f);
                    //要检测的图片
/*                String filePath = path+"\\"+name+".jpeg";
                System.out.println("test:"+filePath);
                //  String filePath = "resources\5.jpeg";
                //图片转base64字符串处理
                String base64Img = Base64Util.encode(FileUtil.readFileByBytes(filePath));
               //参数对象转JSON字符串
                FaceRequest bean = new FaceRequest();
                bean.setImage_type(IMAGEBASE64);
                bean.setImage(base64Img);
                //查询的属性 age,beauty,expression,face_shape,gender,glasses,landmark,landmark150,race,
                // quality,eye_status,emotion,face_type,mask信息
                // 逗号分隔. 默认只返回face_token、人脸框、概率和旋转角度
                bean.setFace_field("age,beauty,mask");
                String param = JSON.toJSONString(bean);
                //发送请求并获取结果
                String result = HttpUtil.post(REFUSE_URL+"?access_token="+accessToken, param);
                //打印检测结果
                System.out.println(result);
                //给图片画框
                getReact(filePath, JSON.parseObject(result, FaceMaskSample.class));
          */  } catch (IOException e) {
                    e.printStackTrace();
                }



                panel.repaint();
                //n++;
                //break;
            }
            capture.release();
            frame.dispose();
        }catch(Exception e){
            System.out.println("例外：" + e);
        }finally{
            System.out.println("--done--");
        }

    }

    /**
     * opencv实现人脸识别
     * @param img
     */

    public static Mat detectFace(Mat img) throws Exception
    {

        System.out.println("Running DetectFace ... ");
        // 从配置文件lbpcascade_frontalface.xml中创建一个人脸识别器，该文件位于opencv安装目录中
        CascadeClassifier faceDetector = new CascadeClassifier("C:\\IDEA\\OpenCV\\opencv\\build\\etc\\haarcascades\\haarcascade_frontalface_alt.xml");

        // 在图片中检测人脸
        MatOfRect faceDetections = new MatOfRect();

        faceDetector.detectMultiScale(img, faceDetections);

        //System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        Rect[] rects = faceDetections.toArray();
        if(rects != null && rects.length >= 1){
            for (Rect rect : rects) {
                Imgproc.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255,0, 0), 2);
        }
            //拍照
   /*             SimpleDateFormat data = new SimpleDateFormat("yyyyMMddHHmmss");
            String name = data.format(new Date());
            //获取桌面路径
            //File path = FileSystemView.getFileSystemView().getHomeDirectory();
            String path = "C:\\E盘(文件)\\IdeaProjects\\FaceMaskSample\\images";
            System.out.println(path);
            System.out.println(name);
            String format = "jpeg";
            File f = new File(path + File.separator + name + "." + format);
           try {
                ImageIO.write(mImg, format, f);
                //要检测的图片
                String filePath = path+"\\"+name+".jpeg";
               System.out.println("test:"+filePath);
                //  String filePath = "resources\5.jpeg";
                //图片转base64字符串处理
                String base64Img = Base64Util.encode(FileUtil.readFileByBytes(filePath));
                //参数对象转JSON字符串
                FaceRequest bean = new FaceRequest();
                bean.setImage_type(IMAGEBASE64);
                bean.setImage(base64Img);
                //查询的属性 age,beauty,expression,face_shape,gender,glasses,landmark,landmark150,race,
                // quality,eye_status,emotion,face_type,mask信息
                // 逗号分隔. 默认只返回face_token、人脸框、概率和旋转角度
                bean.setFace_field("age,beauty,mask");
                String param = JSON.toJSONString(bean);
                //发送请求并获取结果
                 String result = HttpUtil.post(REFUSE_URL+"?access_token="+accessToken, param);
                //打印检测结果
                System.out.println(result);
                //给图片画框
                getReact(filePath, JSON.parseObject(result, FaceMaskSample.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
*/
        }else
        {
            System.out.println("N");
        }
        return img;
    }


    /**
     * opencv实现人型识别，hog默认的分类器。所以效果不好。
     * @param img
     */

    public static Mat detectPeople(Mat img) {
        //System.out.println("detectPeople...");
        if (img.empty()) {
            System.out.println("image is exist");
        }
        HOGDescriptor hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
        System.out.println(HOGDescriptor.getDefaultPeopleDetector());
        //hog.setSVMDetector(HOGDescriptor.getDaimlerPeopleDetector());
        MatOfRect regions = new MatOfRect();
        MatOfDouble foundWeights = new MatOfDouble();
        //System.out.println(foundWeights.toString());
        hog.detectMultiScale(img, regions, foundWeights);
        for (Rect rect : regions.toArray()) {
            Imgproc.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),new Scalar(0, 0, 255), 2);
        }
        return img;
    }

}

