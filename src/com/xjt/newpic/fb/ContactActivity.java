
package com.xjt.newpic.fb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.UserInfo;
import com.xjt.newpic.R;

/**
 * Activity for user to fill plain messy contact information. Developers are
 * encouraged to implement their own activities by following this example using
 * the API provided by SDK.
 * 
 * @author lucas
 * 
 */
public class ContactActivity extends Activity {

    /**
     * The predefined key used by Umeng Feedback SDK to store non-structural
     * plain text messy contact info. Info can be retrieved from
     * {@link UserInfo#getContact()} with key
     * {@link #KEY_UMENG_CONTACT_INFO_PLAIN_TEXT}. <br />
     * This key is reserved by Umeng. Third party developers DO NOT USE this
     * key.
     */
    private static final String KEY_UMENG_CONTACT_INFO_PLAIN_TEXT = "plain";

    private ImageView backBtn;
    private ImageView saveBtn;

    private EditText contactInfoEdit;
    private FeedbackAgent agent;

    private TextView lastUpdateAtText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.umeng_fb_activity_contact);
        agent = new FeedbackAgent(this);

        backBtn = (ImageView) this.findViewById(R.id.umeng_fb_back);
        saveBtn = (ImageView) this.findViewById(R.id.umeng_fb_save);
        contactInfoEdit = (EditText) this
                .findViewById(R.id.umeng_fb_contact_info);
        lastUpdateAtText = (TextView) this
                .findViewById(R.id.umeng_fb_contact_update_at);

        try {
            String contact_info = agent.getUserInfo().getContact()
                    .get(KEY_UMENG_CONTACT_INFO_PLAIN_TEXT);
            contactInfoEdit.setText(contact_info);

            long time = agent.getUserInfoLastUpdateAt();

            if (time > 0) {
                Date date = new Date(time);
                String prefix = this.getResources().getString(R.string.umeng_fb_contact_update_at);
                lastUpdateAtText.setText(prefix
                        + SimpleDateFormat.getDateTimeInstance().format(date));
                lastUpdateAtText.setVisibility(View.VISIBLE);
            } else {
                lastUpdateAtText.setVisibility(View.GONE);
            }

            // If user has never entered any contact information, request focus
            // on the edittext on startup.
            if (TextUtils.isEmpty(contact_info)) {
                contactInfoEdit.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        backBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                back();
            }

        });
        saveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    UserInfo info = agent.getUserInfo();
                    if (info == null)
                        info = new UserInfo();
                    Map<String, String> contact = info.getContact();
                    if (contact == null)
                        contact = new HashMap<String, String>();
                    String contact_info = contactInfoEdit.getEditableText()
                            .toString();
                    contact.put(KEY_UMENG_CONTACT_INFO_PLAIN_TEXT, contact_info);
                    info.setContact(contact);

                    //					Map<String, String> remark = info.getRemark();
                    //					if (remark == null)
                    //						remark = new HashMap<String, String>();
                    //					remark.put("tag1", "game");
                    //					info.setRemark(remark);

                    agent.setUserInfo(info);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                back();
            }

        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @SuppressLint("NewApi")
    void back() {
        finish();
    }
}
