package com.aibots.wkimdemo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannelType;
import com.xinbida.wukongim.entity.WKMsg;
import com.xinbida.wukongim.message.type.WKConnectStatus;
import com.xinbida.wukongim.msgmodel.WKTextContent;

public class MainActivity extends AppCompatActivity {

    private Button sendButton, clearButton, connButton;

    private TextView tvLogger;

    private EditText tvHost, tvUserID, tvToken, tvSendTo, tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.btnSend);
        sendButton.setOnClickListener(view -> {
            WKTextContent textContent = new WKTextContent(tvMsg.getText().toString());
            WKIM.getInstance().getMsgManager().sendMessage(textContent, tvSendTo.getText().toString(), WKChannelType.PERSONAL);
        });

        tvLogger = findViewById(R.id.tvLogger);
        tvHost = findViewById(R.id.tvHost);
        tvUserID = findViewById(R.id.tvUserID);
        tvToken = findViewById(R.id.tvToken);
        tvSendTo = findViewById(R.id.tvSendTo);
        tvMsg = findViewById(R.id.tvMsg);

        connButton = findViewById(R.id.btnConnect);
        connButton.setOnClickListener(view -> initWuKongIM());

        clearButton = findViewById(R.id.btnClear);
        clearButton.setOnClickListener(view -> tvLogger.setText(""));
    }

    private void logger(String info) {
        String msg = (String) tvLogger.getText();
        msg += info + "\n";
        tvLogger.setText(msg);

    }

    private void initWuKongIM() {

        // 初始化sdk
        WKIM.getInstance().init(this, tvUserID.getText().toString(), tvToken.getText().toString());

        // 初始化IP
        WKIM.getInstance().getConnectionManager().addOnGetIpAndPortListener(iGetSocketIpAndPortListener -> {
            // 可请求接口后返回到sdk
            iGetSocketIpAndPortListener.onGetSocketIpAndPort(tvHost.getText().toString(), 5100);
        });

        // 连接服务端
        WKIM.getInstance().getConnectionManager().connection();


        // 连接状态监听
        WKIM.getInstance().getConnectionManager().addOnConnectionStatusListener("listener_key", (i, s) -> {
            // 0 失败 【WKConnectStatus.fail】
            // 1 成功 【WKConnectStatus.success】
            // 2 被踢 【WKConnectStatus.kicked】
            // 3 同步消息中【WKConnectStatus.syncMsg】
            // 4 连接中 【WKConnectStatus.connecting】
            // 5 无网络连接 【WKConnectStatus.noNetwork】
            logger(s);
            switch (i) {
                case WKConnectStatus.fail:
                case WKConnectStatus.success:
                case WKConnectStatus.kicked:
                case WKConnectStatus.syncMsg:
                case WKConnectStatus.connecting:
                case WKConnectStatus.noNetwork:
                    break;
            }
        });

        // 发送消息结果监听
        WKIM.getInstance().getMsgManager().addOnSendMsgAckListener("listener_key", wkMsg -> logger("发送消息结果监听: " + wkMsg.content));

        // 监听新消息
        WKIM.getInstance().getMsgManager().addOnNewMsgListener("listener_key", list -> {
            for (WKMsg msg : list) {
                logger("监听新消息: " + msg.content);
            }
        });

        // 命令消息(cmd)监听
        WKIM.getInstance().getCMDManager().addCmdListener("listener_key", cmd -> logger("命令消息: " + cmd.cmdKey));
    }
}