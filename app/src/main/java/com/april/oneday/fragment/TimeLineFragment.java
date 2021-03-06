package com.april.oneday.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.april.oneday.R;
import com.april.oneday.activity.EditActivity;
import com.april.oneday.activity.FullImageActivity;
import com.april.oneday.activity.MainActivity;
import com.april.oneday.adapter.TimeLineAdapter;
import com.april.oneday.bean.MediaInfo;
import com.april.oneday.dao.MediaDao;
import com.april.oneday.utils.MyAsycnTaks;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.makeramen.segmented.SegmentedRadioGroup;

import java.util.List;

/**
 * Created by wangtongyu on 2016/4/18.
 */
public class TimeLineFragment extends Fragment {

    private static final int REQUEST_CODE_FULLIMAGE =100 ;
    private static final int REQUEST_CODE_EDIT =1000 ;
    MainActivity mActivity;
    private List<MediaInfo> list;
    private MediaDao dao;
    private TimeLineAdapter timeLineAdapter;
    private ListView lv_timeline;

    //查询的总个数
    private final int MAXNUM=10;
    //起始位置
    private int startIndex = 0;
    private ImageButton ib_timeline;
    private String basepath;
    private int clickedItem;
    private Button btnMenu;
    private SegmentedRadioGroup rg_option;
    private RadioButton rb_timeline;

    public TimeLineFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dao = new MediaDao(getActivity());
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_timeline, container,false);
        rg_option = (SegmentedRadioGroup) inflate.findViewById(R.id.rg_option);
        rb_timeline = (RadioButton) inflate.findViewById(R.id.rb_timeline);
        btnMenu = (Button) inflate.findViewById(R.id.btn_titalbar_menu);
        lv_timeline = (ListView) inflate.findViewById(R.id.lv_timeline);
        /*找到添加键*/
        ib_timeline = (ImageButton) inflate.findViewById(R.id.ib_timeline);
        fillData();
        lv_timeline.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    //获取界面显示最后一个条目
                    int position = lv_timeline.getLastVisiblePosition();//获取界面显示最后一个条目,返回的时候条目的位置
                    //判断是否是查询数据的最后一个数据  20   0-19
                    if (position == list.size()-1) {
                        //加载下一波数据
                        //更新查询的其实位置   0-19    20-39
                        startIndex+=MAXNUM;
                        fillData();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });

        lv_timelineOscroll();
        rgOptionOnclick();
        initSlidingMenu();
        btnMenuOnClick();
        InitEvent();
        return inflate;
    }

    /**
     * 菜单按钮点击事件,展开关闭侧边栏
     */
    private void btnMenuOnClick() {
        this.btnMenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                mActivity.getSlidingMenu().toggle();
            }

        });
    }

    /**
     * listView滑动监听
     */

    private void lv_timelineOscroll() {
        lv_timeline.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    //获取界面显示最后一个条目
                    int position = lv_timeline.getLastVisiblePosition();//获取界面显示最后一个条目,返回的时候条目的位置
                    //判断是否是查询数据的最后一个数据  20   0-19
                    if (position == list.size()-1) {
                        //加载下一波数据
                        //更新查询的其实位置   0-19    20-39
                        startIndex+=MAXNUM;
                        fillData();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }


    /**
     * radiogroup点击监听
     */
    private void rgOptionOnclick() {
        rg_option.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup paramRadioGroup, int paramInt) {
                mActivity.showSchedule();
                initSlidingMenu();
            }
        });
    }

    /**
     * 初始化侧边栏
     */
    public void initSlidingMenu() {
        SlidingMenu menu = mActivity.getSlidingMenu();
        mActivity.setBehindContentView(R.layout.slidingmenu_timeline);
        menu.setBehindOffset(200);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
    }

    /**
     * fragment隐藏显示状态监听
     */
    public void onHiddenChanged(boolean hidden)
    {

        if (this.rb_timeline != null){
            rb_timeline.toggle();
        }

    }


    //初始化数据
    public void fillData(){
        new MyAsycnTaks(){

            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                //从数据库查询数据
                if (list==null){
                    list = dao.queryPartMediaInfo(MAXNUM, startIndex);
                }else {
                    list.addAll(dao.queryPartMediaInfo(MAXNUM,startIndex));
                }

            }

            @Override
            public void postTask() {
                //给listView设置适配器
                if (timeLineAdapter==null){
                    timeLineAdapter = new TimeLineAdapter(list,mActivity);
                    lv_timeline.setAdapter(timeLineAdapter);
                } else {
                    timeLineAdapter.notifyDataSetChanged();
                }

            }
        }.execute();
    }

    /**
     * 设置add按钮的点击事件和listview的Item点击事件
     */
    public void InitEvent()
    {
        ib_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mActivity,EditActivity.class),REQUEST_CODE_EDIT);
            }
        });

        lv_timeline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickedItem =position;
                Intent intent = new Intent(mActivity, FullImageActivity.class);

                MediaInfo info = list.get(position);

                Bundle bundle = new Bundle();
                bundle.putSerializable("mediainfo",info);
                intent.putExtras(bundle);
                startActivityForResult(intent,REQUEST_CODE_FULLIMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100&&resultCode==500)
        {
            list.remove(clickedItem);
            timeLineAdapter.notifyDataSetChanged();
        }
        else if (requestCode==1000)
        {
            startIndex=0;
            list.clear();
            fillData();
        }
    }
}
