package com.ainikuzs;

import android.R.string;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

/**
 * <p>
 * Created by Administrator
 * </p>
 * <p/>
 * 抢红包外挂服务
 */
public class EnvelopeService extends AccessibilityService {

	static final String TAG = "keliserver";
	// 锁屏、解锁相关
	private boolean enableKeyguard = true;// 默认有屏幕锁
	private KeyguardManager km;
	@SuppressWarnings("deprecation")
	private KeyguardLock kl;
	// 唤醒屏幕相关
	private PowerManager pm;
	private PowerManager.WakeLock wl = null;
	// 播放提示声音
	private MediaPlayer player;
	/**
	 * 微信的包名
	 */
	static final String WECHAT_PACKAGENAME = "com.tencent.mm";
	/**
	 * 红包消息的关键字
	 */
	static final String ENVELOPE_TEXT_KEY = "红包";

	Handler handler = new Handler();

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		final int eventType = event.getEventType();

		Log.d(TAG, "事件---->" + event);
		Log.d("runtimeing", "事件---->" + event);
		// 通知栏事件
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				for (CharSequence t : texts) {
					String text = String.valueOf(t);
					if (text.contains(ENVELOPE_TEXT_KEY)) {
						openNotification(event);
						break;
					}
				}
			}
			
		} else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			//窗口的状态或内容改变的时候
			Log.d("runtimeing2", "事件---->" + event);
			openEnvelope(event);
		}
	}

	/*
	 * @Override protected boolean onKeyEvent(KeyEvent event) { //return
	 * super.onKeyEvent(event); return true; }
	 */

	@Override
	public void onInterrupt() {
		Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
	}

	private void sendNotificationEvent() {
		AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
		if (!manager.isEnabled()) {
			return;
		}
		AccessibilityEvent event = AccessibilityEvent
				.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
		event.setPackageName(WECHAT_PACKAGENAME);
		event.setClassName(Notification.class.getName());
		CharSequence tickerText = ENVELOPE_TEXT_KEY;
		event.getText().add(tickerText);
		manager.sendAccessibilityEvent(event);
	}

	/**
	 * 打开通知栏消息
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openNotification(AccessibilityEvent event) {
		if (event.getParcelableData() == null
				|| !(event.getParcelableData() instanceof Notification)) {
			return;
		}
		// 以下是精华，将微信的通知栏消息打开
		Notification notification = (Notification) event.getParcelableData();
		PendingIntent pendingIntent = notification.contentIntent;
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}

	//窗口改变后判断是不是微信的界面
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openEnvelope(AccessibilityEvent event) {
		CharSequence str=event.getClassName();
		if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(str)) {
			// 点中了红包，下一步就是去拆红包
			chaihongbao();
		} else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(str)) {
			// 拆完红包后看详细的纪录界面
			// nonething
		} else if ("com.tencent.mm.ui.LauncherUI".equals(str)) {
			// 在聊天界面,去点中红包
			clickhongbao();
		}else{
			//查看最后打开的红包
			checkhongbao();
		}
		
	}

	// 通过文本查找节点
	@SuppressLint("NewApi")
	public AccessibilityNodeInfo findNodeInfosByText(
			AccessibilityNodeInfo nodeInfo, String text) {
		List<AccessibilityNodeInfo> list = nodeInfo
				.findAccessibilityNodeInfosByText(text);
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	// 模拟点击事件
	@SuppressLint({ "NewApi", "InlinedApi" })
	public void performClick(AccessibilityNodeInfo nodeInfo) {
		if (nodeInfo == null) {
			return;
		}
		if (nodeInfo.isClickable()) {
			nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		} else {
			performClick(nodeInfo.getParent());
		}
	}

	// 模拟返回事件
	@SuppressLint("NewApi")
	public void performBack(AccessibilityService service) {
		if (service == null) {
			return;
		}
		service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
	}

	//查看最后打开的红包
	@SuppressLint("NewApi")
	private void checkhongbao(){
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			return;
		}
		Log.i("demo", "查找打开按钮...");
		AccessibilityNodeInfo targetNode = null;
		// 如果红包已经被抢完则直接返回
		targetNode = findNodeInfosByText(nodeInfo, "開");
		// 通过组件名查找开红包按钮，还可通过组件id直接查找但需要知道id且id容易随版本更新而变化，旧版微信还可直接搜“開”字找到按钮
		if (targetNode == null) {
			Log.i("demo", "打开按钮中...");
			for (int i = 0; i < nodeInfo.getChildCount(); i++) {
				AccessibilityNodeInfo node = nodeInfo.getChild(i);
				if ("android.widget.Button".equals(node.getClassName())) {
					targetNode = node;
					break;
				}
			}
		}
		// 若查找到打开按钮则模拟点击
		if (targetNode != null) {
			final AccessibilityNodeInfo n = targetNode;
			// 播放提示音
			playSound(this);
			performClick(n);
		}
	}
	
	//拆红包
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void chaihongbao() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			Log.w(TAG, "rootWindow为空");
			return;
		}
		List<AccessibilityNodeInfo> list = nodeInfo
				.findAccessibilityNodeInfosByText("拆红包");
		for (AccessibilityNodeInfo n : list) {
			n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
	}

	//点击红包
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void clickhongbao() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			Log.w(TAG, "rootWindow为空");
			return;
		}
		List<AccessibilityNodeInfo> list = nodeInfo
				.findAccessibilityNodeInfosByText("领取红包");
		if (list.isEmpty()) {
			list = nodeInfo.findAccessibilityNodeInfosByText(ENVELOPE_TEXT_KEY);
			for (AccessibilityNodeInfo n : list) {
				Log.i(TAG, "-->微信红包:" + n);
				n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				break;
			}
		} else {
			// 最新的红包领起
			for (int i = list.size() - 1; i >= 0; i--) {
				AccessibilityNodeInfo parent = list.get(i).getParent();
				Log.i(TAG, "-->领取红包:" + parent);
				if (parent != null) {
					parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					break;
				}
			}
		}
	}

	// 唤醒屏幕和解锁
	@SuppressWarnings("deprecation")
	private void wakeAndUnlock(boolean unLock) {
		if (unLock) {
			// 若为黑屏状态则唤醒屏幕
			if (!pm.isScreenOn()) {
				// 获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
				wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
				// 点亮屏幕
				wl.acquire();
				Log.i("demo", "亮屏");
			}
			// 若在锁屏界面则解锁直接跳过锁屏
			if (km.inKeyguardRestrictedInputMode()) {
				// 设置解锁标志，以判断抢完红包能否锁屏
				enableKeyguard = false;
				// 解锁
				kl.disableKeyguard();
				Log.i("demo", "解锁");
			}
		} else {
			// 如果之前解过锁则加锁以恢复原样
			if (!enableKeyguard) {
				// 锁屏
				kl.reenableKeyguard();
				Log.i("demo", "加锁");
			}
			// 若之前唤醒过屏幕则释放之使屏幕不保持常亮
			if (wl != null) {
				wl.release();
				wl = null;
				Log.i("demo", "关灯");
			}
		}
	}

	public void playSound(Context context) {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		// 夜间不播放提示音
		if (hour > 7 && hour < 22) {
			player.start();
		}
	}
}