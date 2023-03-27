package com.example.elebicycle.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color

import android.widget.Toast
import com.example.elebicycle.MainActivity
import com.example.elebicycle.ui.bicycleInfo.InfoViewModel
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter
import lecho.lib.hellocharts.gesture.ContainerScrollType
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.text.SimpleDateFormat
import java.util.*

class LineChart(val mainActivity: MainActivity) {

    private lateinit var lineChart: LineChartView
    private var pointValueList: ArrayList<PointValue>? = null
    private var linesList: ArrayList<Line>? = null
    private var mAxisXValues: ArrayList<AxisValue>? = null
    private var timeList: ArrayList<String>? = null
    private var simpleDateFormat: SimpleDateFormat? = null

    private var numberOfPoints = 0 //用来记录当前折线图有多少个点，同时可以作为判断的下标

    private var hasLines = true

    private var hasPoints = true

    private var hasLabels = true //是否显示点的数据

    private var value = 0f
    private var isChange = false
    var timer: Timer? = null
    var timerTask: TimerTask? = null
    private var isRun = true

    //Y轴
    private lateinit var axisY: Axis
    private var yName = ""
    private var yHeight = 20 //y轴高度
    private var yGap = 5 //y轴间隔

    //判定接收哪个类型值作为y轴值
    private var whichValue = ""

    //X轴
    private lateinit var axisX: Axis
    private var xName = "时间（单位：s）"
    private var xRegionMax = 6 //x轴最多展示的点

    //折线图数据模型
    private var data: LineChartData? = null

    //广播动作
    var broadAction = ""
    var receiver: BroadcastReceiver? = null
    var receiver1: BroadcastReceiver? = null
    var receiver2: BroadcastReceiver? = null

    //设置X轴最大显示数
    fun setXMax(xRegeion: Int) {
        this.xRegionMax = xRegeion
    }

    //设置y轴名称
    fun setYName(yName: String) {
        this.yName = yName
    }

    //设置x轴名称
    fun setXName(xName: String) {
        this.xName = xName
    }

    //设置Y轴高度及其间隔
    fun setYHight(yHeight: Int, yGap: Int) {
        this.yHeight = yHeight
        this.yGap = yGap
    }

    //设置折线图
    fun setChart(chart:LineChartView){
        this.lineChart = chart
    }

    fun showChart() {


//      初始化界面
        initView()

        initViewport()

        lineChart.isViewportCalculationEnabled = false

        //按时间自动走
        startAClock()

        //创建广播
        createBroadcast()

        //返回按钮点击事件

    }

    private fun initView() {
        //设置值触碰时的监听器
        lineChart.onValueTouchListener = object : LineChartOnValueSelectListener {
            override fun onValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue) {
                Toast.makeText(mainActivity, "Selected: $value", Toast.LENGTH_SHORT).show()
            }

            override fun onValueDeselected() {
                // TODO Auto-generated method stub
            }
        }
        initData()
    }

    @SuppressLint("SimpleDateFormat")
    private fun initData() {
        pointValueList = ArrayList<PointValue>()
        linesList = ArrayList<Line>()
        timeList = ArrayList<String>()
        simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        data = LineChartData(linesList) //初始化data
    }

    //开始按时间执行
    private fun startAClock() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                if (isRun) {
                    showMovingLineChart()
                }
            }
        }

        //每隔1秒执行1次
        timer?.scheduleAtFixedRate(timerTask, 0, 3000)
    }

    fun setWhichValue(choose:String){
        this.whichValue = choose
    }

    fun getWhichValue():String{
        return this.whichValue
    }

    private fun generateValues(): PointValue {

        //isChange用于判断是否接收到了值，若接受到了就为false,否则就为true按默认值0自动走
        if (!isChange) { //如果未处于变化状态，值就默认为0
            when(getWhichValue()) {
                "temp" -> {
                    value = InfoViewModel.getTem().toFloat()
                }
                "V" -> {
                    value = InfoViewModel.getV().toFloat()
                }
            }

        }
        isChange = false //赋值后置为false

        return PointValue(numberOfPoints++.toFloat(), value)
    }

    //展示移动中的视图窗口
    private fun showMovingLineChart() {
        val pointValue = generateValues()

        pointValueList?.add(pointValue) //实时添加新的点

        //点集超过了,就删掉看不到的点
        if (numberOfPoints > xRegionMax + 1) {
            pointValueList?.removeAt(0)
        }

        //根据新的点的集合画出新的线
        val line = Line(pointValueList)

        //保留2位小数
        val linechartValueFormatter = SimpleLineChartValueFormatter(3)

        line.apply {
            color = Color.parseColor("#ff33b5e5") //设置折线颜色
            shape =
                ValueShape.CIRCLE //设置折线图上数据点形状为 圆形 （共有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
            isCubic = true//曲线是否平滑，true是平滑曲线，false是折线
            setHasLabels(hasLabels) //数据是否有标注
            setHasLines(hasLines) //是否用线显示，如果为false则没有曲线只有点显示
            setHasPoints(hasPoints) //是否显示圆点 ，如果为false则没有原点只有点显示（每个数据点都是个大圆点）
            isFilled = true //是否填充
            formatter = linechartValueFormatter
        }

        linesList?.add(line)
        /**
         *  只能说这一步是神来之笔，大家可以试一下有无下面的移除内存的变化程度0.0
         *  因为line存储的数据太多了，如果让linesList不断增大的话，不一会儿就崩了
         */
        if (numberOfPoints > xRegionMax) {
            linesList?.removeAt(0);
        }

        data?.lines = linesList //设置变化后的线集合
        data?.axisYLeft = axisY//设置Y轴在左

        data?.axisXBottom = axisX//X轴在底部
        lineChart.lineChartData = data

        //下面的代码用来滚动横坐标
        val port: Viewport = if (numberOfPoints > xRegionMax) {
            initViewPort(numberOfPoints - xRegionMax, numberOfPoints)
        } else {
            initViewPort(0, xRegionMax)
        }

        lineChart.maximumViewport = port
        lineChart.currentViewport = port

    }

    //最原始的视图窗口初始化
    private fun initViewport() {
        /** 初始化Y轴 */
        axisY = Axis()

        //设置Y轴的一系列属性
        axisY.apply {
            name = yName//添加Y轴的名称
//            setHasLines(true) //Y轴分割线
            textSize = 12 //设置字体大小
            maxLabelChars = 3
            lineColor = Color.rgb(90, 183, 223)
            textColor = Color.rgb(90, 183, 223) //设置Y轴颜色，默认浅灰色
        }

        //Y轴的值集合
        val mAxisYValues = ArrayList<AxisValue>()

        for (i in 0..yHeight step yGap) { //此处封装一下,Y轴的标刻
            val aValue = AxisValue(i.toFloat())
            mAxisYValues.add(aValue.setLabel("" + i))//带标签的值
        }

        axisY.values = mAxisYValues  //填充Y轴的坐标

        data?.axisYLeft = axisY  //设置Y轴在左边

        /** 初始化X轴 */
        axisX = Axis()
        axisX.apply {
            setHasTiltedLabels(false) //X坐标轴字体是斜的显示还是直的，true是斜的显示
            textColor = Color.rgb(90, 183, 223) //设置X轴颜色
            name = xName//X轴名称  此处也要封装一下
//            setHasLines(true) //X轴分割线
            lineColor = Color.rgb(90, 183, 223)
            textSize = 10 //设置字体大小
            maxLabelChars = 0 //设置0的话X轴坐标值就间隔为1
        }

        //给x轴设置时间轴
        mAxisXValues = ArrayList<AxisValue>() //x轴值集合
        val time = System.currentTimeMillis() //获取当前系统时间戳

        var y = 0
        for (x in 0..999 ) {
            y+= 3 //让时间刻度每3秒变一次
            simpleDateFormat?.format(Date(time + 1000 * y))?.let { timeList?.add(it) }
            mAxisXValues?.add(AxisValue(x.toFloat()).setLabel(timeList?.get(x))) //横坐标值带标签
        }

        axisX.values = mAxisXValues
        data?.axisXBottom = axisX //X轴在底部

        //设置该折线图的数据模型
        lineChart.lineChartData = data

        val port: Viewport = initViewPort(0, xRegionMax) //初始化X轴10个间隔坐标

        //折线图的一些属性设置
        lineChart.apply {
            setCurrentViewportWithAnimation(port)
            isInteractive = false //设置不可交互
            isScrollEnabled = true
            isValueTouchEnabled = true
            isFocusableInTouchMode = false
            isViewportCalculationEnabled = false
            setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
            startDataAnimation()
        }

    }

    //改变视图窗口(带参数)
    private fun initViewPort(left: Int, right: Int): Viewport {
        val port = Viewport()
        port.top = yHeight.toFloat() //Y轴上限，固定(不固定上下限的话，Y轴坐标值可自适应变化) 此处也要封装出来

        port.bottom = 0f //Y轴下限，固定

        port.left = left.toFloat() //X轴左边界，变化

        port.right = right.toFloat() //X轴右边界，变化

        return port

    }

    fun setBroadcast(action:String){
        this.broadAction = action
    }

    private fun createBroadcast() {
        //用广播获取传递过来的数据
        val filter = IntentFilter()
        filter.addAction(broadAction) //动态注册广播
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action == broadAction) {
                    setValue(intent.getFloatExtra("value", 0f))
                    showMovingLineChart()
                }
            }
        }

        mainActivity.registerReceiver(receiver, filter)

        //用广播接收暂停的动作
        val filter1 = IntentFilter()
        filter1.addAction("com.example.trafficdetection.stop")
        receiver1 = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action.equals("com.example.trafficdetection.stop")) {
                    stop()
                }
            }
        }
        mainActivity.registerReceiver(receiver1, filter1)

        //用广播接收开始的动作
        val filter2 = IntentFilter()
        filter2.addAction("com.example.trafficdetection.start")
        receiver2 = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action.equals("com.example.trafficdetection.start")) {
                    //重置各界面
                    restart()
                }
            }
        }
        mainActivity.registerReceiver(receiver2, filter2)

    }

    private fun setValue(x: Float) {
        this.isChange = true //发生改变，就不会使用默认的值
        this.value = x //将广播传来的值设置为当前的值
    }

    private fun stop() {
        isRun = false
    }

    //重置所有数据
    private fun reset() {
        numberOfPoints = 0
        initData()
        initViewport()
    }

    private fun restart() {

        timerTask?.cancel()
        timerTask = null

        timer?.cancel()
        timer = null

        isRun = true //将运行状态设为true
        reset()
        startAClock()
    }

    //清除一切，并注销广播接收器
    fun destroy() {

        lineChart.clearFocus()
        lineChart.clearAnimation()

        linesList?.clear()

        timeList?.clear()

        //注销广播
        mainActivity.let {
            it.unregisterReceiver(receiver)
            it.unregisterReceiver(receiver1)
            it.unregisterReceiver(receiver2)
        }
    }
}