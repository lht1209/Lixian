package com.example.lyzy.lixian;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geodatabase.GeodatabaseFeature;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Main extends Activity
{
    RasterLayer rasterLayer;
    String rasterPath = Environment.getExternalStorageDirectory().getPath() + "/test/ligong.tif";
    private static final String TAG = null;
    FileRasterSource rasterSource = null;
    MapView mapView = null;
    TextView textView;
    ImageView imageView;
    ImageButton btnzo;
    ImageButton btnzi;
    ImageButton btnc;
    Button btnbj;
    private boolean isVisible = true;
    Button btnhz;
    Button btncx;
    Button btnwc;
    Button btnjp;
//     传感器管理器
    private SensorManager manager;
    private SensorListener listener;
    GraphicsLayer graphicsLayer;
    Point startPoint = null;
    //    存储绘制 线 面 轨迹数据
    MultiPath multiPath ;
    MultiPath multiPath1 ;
    boolean start = false;
    boolean close = false;
    boolean startcx = false;
    boolean startpolyline= false;
    SimpleLineSymbol simpleLineSymbol;
    int addgraphicidl;
    int addgraphicidg;
    boolean vertexselected = false;
    GraphicsLayer graphicsLayerEditing;
    GraphicsLayer graphicsLayeredit;
    GraphicsLayer highlightGraphics;
    boolean featureUpdate = false;
    long featureUpdateId;
    MyTouchListener listener1 = null;
    String fid;
    String field = null;
    String area = null;
    EditText editText1;
    EditText editText2;
    EditText editText3;
    ArrayList<Point> points = null;
    int uid;



    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //去除水印
        ArcGISRuntime.setClientId("1eFHW78avlnRUPHm");

        mapView = (MapView) findViewById(R.id.map);
        //        editText = (EditText) findViewById(R.id.scale);
        textView = (TextView) findViewById(R.id.scalet);

        btnzo = (ImageButton) findViewById(R.id.btnzo);
        btnzi = (ImageButton) findViewById(R.id.btnzi);
        btnc = (ImageButton) findViewById(R.id.btnc);
        btnbj = (Button) findViewById(R.id.button);


        btnhz = (Button) findViewById(R.id.button2);
        btnhz.setVisibility(View.INVISIBLE);
        btncx = (Button) findViewById(R.id.button6);
        btncx.setVisibility(View.INVISIBLE);
        btnwc = (Button) findViewById(R.id.button7);
        btnwc.setVisibility(View.INVISIBLE);
        btnjp = (Button) findViewById(R.id.button8);
        btnjp.setVisibility(View.INVISIBLE);

        imageView = (ImageView) findViewById(R.id.imageView);

        listener = new SensorListener();
        imageView.setKeepScreenOn(true);//屏幕高亮
        //获取系统服务（SENSOR_SERVICE)返回一个SensorManager 对象
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        try
        {
            rasterSource = new FileRasterSource(rasterPath);
        } catch (IllegalArgumentException ie)
        {
            Log.d(TAG, "null or empty path");
        } catch (FileNotFoundException fe)
        {
            Log.d(TAG, "raster file doesn't exist");
        } catch (RuntimeException re)
        {
            Log.d(TAG, "raster file can't be opened");
        }
        rasterLayer = new RasterLayer(rasterSource);
        mapView.addLayer(rasterLayer);
        graphicsLayer = new GraphicsLayer();
        mapView.addLayer(graphicsLayer);
        graphicsLayeredit = new GraphicsLayer();
        mapView.addLayer(graphicsLayeredit);

        //        Envelope envelope = new Envelope(3524505.00866, 635997.19492, 642159.19492, 3519623.00866);
        //        mapView.setExtent(envelope);
//      比例尺
        mapView.setOnStatusChangedListener(new OnStatusChangedListener()
        {
            private static final long serialVersionUID = 1L;

            public void onStatusChanged(Object source, STATUS status)
            {
                if (OnStatusChangedListener.STATUS.INITIALIZED == status && source == mapView)
                {
                    SpatialReference spatialReference = mapView.getSpatialReference();
                    double d = mapView.getResolution();
                    double s = mapView.getScale();
                    long sl = Math.round(s);
                    //                    editText.setText("比例尺1:" + sl);
                    textView.setText("比例尺1:" + sl);
                    Log.i("Test", "resolution:" + spatialReference + d + s);
                }
            }
        });
//        比例尺
        mapView.setOnZoomListener(new OnZoomListener()
        {
            @Override
            public void preAction(float v, float v1, double v2)
            {

            }

            @Override
            public void postAction(float v, float v1, double v2)
            {
                double scale = mapView.getScale();
                long scalelong = Math.round(scale);
                //                editText.setText("比例尺1:" + scalelong);
                textView.setText("比例尺1:" + scalelong);
            }

        });

        listener1 = new MyTouchListener(this, mapView);
        mapView.setOnTouchListener(listener1);

        mapView.setOnSingleTapListener(new OnSingleTapListener()
        {
            private static final long serialVersionUID = 1L;

            public void onSingleTap(float x, float y)
            {
                if (startcx)
                {
                    // gets the first 1000 features at the clicked point on the map, within 5 pixels
                    int[] ids = graphicsLayer.getGraphicIDs(x, y, 5, 10);

                    if (ids.length > 0)
                    {
                        Graphic graphicselected = graphicsLayer.getGraphic(ids[0]);
                        graphicsLayer.setSelectionColor(1);
                        fid = String.valueOf(graphicselected.getId()) ;
//                        field = graphicselected.getAttributeValue(field).toString();
//                        area =  graphicselected.getAttributeValue(area).toString();


                        Toast.makeText(getApplicationContext(),
                                "FID=" + fid , Toast.LENGTH_SHORT).show();
//                        customView();
                    } else
                    { //提示无选中要素
                        Log.i("", "无选中要素");
                    }
                }
            }
        });

    }

    public void btnzilistener(View v)
    {
        mapView.zoomin();
    }

    public void btnzolistener(View v)
    {
        mapView.zoomout();
    }





//    拍照
    public void btnclistener(View v)
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    public void btnbjlistener(View v)
    {
        if (isVisible)
        {
            btnhz.setVisibility(View.VISIBLE);
            btnwc.setVisibility(View.VISIBLE);
            btnjp.setVisibility(View.VISIBLE);
            btncx.setVisibility(View.VISIBLE);
            isVisible = false;
        } else
        {
            btnhz.setVisibility(View.INVISIBLE);
            btnwc.setVisibility(View.INVISIBLE);
            btnjp.setVisibility(View.INVISIBLE);
            btncx.setVisibility(View.INVISIBLE);
            isVisible = true;
        }
    }

        @Override
        protected void onResume () {
        /**
         *  获取方向传感器
         *  通过SensorManager对象获取相应的Sensor类型的对象
         */
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //应用在前台时候注册监听器
        manager.registerListener(listener, sensor,
                SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }

        @Override
        protected void onPause () {
        //应用不在前台时候销毁掉监听器
        manager.unregisterListener(listener);
        super.onPause();
    }

        private final class SensorListener implements SensorEventListener
        {

            private float predegree = 0;

            @Override
            public void onSensorChanged(SensorEvent event)
            {
                /**
                 *  values[0]: x-axis 方向加速度
                 　　 values[1]: y-axis 方向加速度
                 　　 values[2]: z-axis 方向加速度
                 */
                float degree = event.values[0];// 存放了方向值
                /**动画效果*/
                RotateAnimation animation = new RotateAnimation(predegree, degree,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(200);
                imageView.startAnimation(animation);
                predegree = -degree;

                /**
                 float x=event.values[SensorManager.DATA_X];
                 float y=event.values[SensorManager.DATA_Y];
                 float z=event.values[SensorManager.DATA_Z];
                 Log.i("XYZ", "x="+(int)x+",y="+(int)y+",z="+(int)z);
                 */
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy)
            {

            }

        }

    public class MyTouchListener extends MapOnTouchListener
    {

        public MyTouchListener(Context context, MapView view)
        {
            //        引用父类方法
            super(context, view);
        }

        //    挡在屏幕上滑动时，将滑动生成的点逐步加入poly变量中；
        @Override
        public boolean onDragPointerMove(MotionEvent from, MotionEvent to)
        {

            if (start)
            {
                Point currentPoint = mapView.toMapPoint(to.getX(), to.getY());
                if (startPoint == null)
                {

                    multiPath = new Polygon();
                    startPoint = mapView.toMapPoint(from.getX(), from.getY());
                    //将第一个点存入multiPath
                    multiPath.startPath((float)startPoint.getX(),(float)startPoint.getY());
                    multiPath.startPath((float) startPoint.getX(), (float) startPoint.getY());
                    uid = graphicsLayeredit.addGraphic(new Graphic(multiPath,new SimpleLineSymbol(Color.BLUE,2)));
                }

                multiPath.lineTo((float)currentPoint.getX(), (float)currentPoint.getY());
                simpleLineSymbol = new SimpleLineSymbol(Color.BLUE, 2);
                //增加线点
                graphicsLayeredit. updateGraphic(uid,multiPath);

//                ArrayList<Point> points = new ArrayList<Point>();
//
//                for (int i = 0; i < multiPath.getPointCount(); i++) {
//                    points.add(multiPath.getPoint(i));
//                }
//
//                for (Point pt : points)
//                {
//                    Graphic graphicp = new Graphic(pt, new SimpleMarkerSymbol(
//                            Color.GREEN, 5, SimpleMarkerSymbol.STYLE.CIRCLE));
//                    int id = graphicsLayeredit.addGraphic(graphicp);
//                }


            }
            return true;
        }

    }



    public void bihelistener(View view){

        SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.RED, SimpleFillSymbol.STYLE.NULL);
        Map<String, Object> attributes = new HashMap<String, Object>();
        //            计算周长
        attributes.put("feild", Math.round(multiPath.calculateLength2D()));
        //            计算面积
        attributes.put("area", Math.abs(Math.round(multiPath.calculateArea2D())));

        Graphic graphicg = new Graphic(multiPath, simpleFillSymbol,attributes);
        //        graphicsLayer.removeGraphic(addgraphicidl);
        graphicsLayeredit.removeAll();
        points = null;
        addgraphicidg = graphicsLayer.addGraphic(graphicg);
        start = false;
        multiPath = null;
        startPoint = null;
    }

    public void btnhzlistener(View view){
        if(!start){
            start=true;
        }else {
            start=false;
        }
    }
    public void shuxingl(View view){
        customView();
    }
    public void xuanzhel(View view){
        if(!startcx){
            startcx=true;
        }else {
            startcx=false;
        }
    }
    public void quxiaol(View view){

    }

    public void customView()
    {
        // 装载app\src\main\res\layout\login.xml界面布局文件
        TableLayout loginForm = (TableLayout)getLayoutInflater()
                .inflate(R.layout.login, null);


        editText1 =(EditText)loginForm.findViewById(R.id.et1);
        editText1.setText(fid);
        editText2 =(EditText)loginForm.findViewById(R.id.et2);
        editText2.setText(field);
        editText3 =(EditText)loginForm.findViewById(R.id.et3);
        editText3.setText(area);

        new AlertDialog.Builder(this)
                // 设置对话框的图标
                //                .setIcon(R.drawable.camera_32)
                // 设置对话框的标题
                .setTitle("属性编辑")
                        // 设置对话框显示的View对象
                .setView(loginForm)
                        // 为对话框设置一个“确定”按钮
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // 此处可执行登录处理
                    }
                })
                        // 为对话框设置一个“取消”按钮
                .setNegativeButton("取消", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which)
                    {
                        // 取消登录，不做任何事情
                    }
                })
                        // 创建并显示对话框
                .create()
                .show();
    }

    private void drawVertices()
    {

        if (graphicsLayeredit == null)
        {
            graphicsLayeredit = new GraphicsLayer();
            mapView.addLayer(graphicsLayeredit);
        }

        for (Point pt : points)
        {

            Graphic graphic = new Graphic(pt, new SimpleMarkerSymbol(
                    Color.RED, 5, SimpleMarkerSymbol.STYLE.CIRCLE));
            Log.d(TAG, "Add Graphic point");
            int id = graphicsLayeredit.addGraphic(graphic);


        }
    }




        //    @Override
        //    protected void onDestroy() {
        //        super.onDestroy();
        //    }
        //    @Override
        //    protected void onPause() {
        //        super.onPause();
        //        mapView.pause();
        //    }
        //    @Override
        //    protected void onResume() {
        //        super.onResume();
        //        mapView.unpause();
        //    }


    }

