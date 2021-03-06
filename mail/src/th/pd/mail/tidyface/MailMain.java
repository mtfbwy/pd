package th.pd.mail.tidyface;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cc.typedef.droid.common.OnActionCallback;
import th.pd.mail.Const;
import th.pd.mail.R;
import th.pd.mail.dao.FastSyncAccess;
import th.pd.mail.dao.MailAcc;
import th.pd.mail.tidyface.compose.ComposeActivity;
import th.pd.mail.tidyface.leftmost.LeftmostFragment;

public class MailMain extends Activity implements OnActionCallback {

    private static final String TAG = MailMain.class.getSimpleName();

    private static final int REQUEST_CODE_SETUP_WIZZARD = 7749;

    private void bindViews() {
        findViewById(R.id.btnCompose).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onAction(R.id.actionRequestCompose, null);
                    }
                });
        findViewById(R.id.btnSync).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onAction(R.id.actionRequestSync, null);
                    }
                });
    }

    @Override
    public boolean onAction(int actionId, Object extra) {
        switch (actionId) {
            case R.id.actionRequestCompose: {
                startActivity(new Intent().setClass(
                        getApplicationContext(),
                        ComposeActivity.class));
                return true;
            }
            case R.id.actionRequestSync: {
                MailAcc acc = (MailAcc) extra;
                if (acc == null) {
                    acc = FastSyncAccess.getMailAccSequence(this)
                            .getCurrent();
                }
                Account account = new Account(acc.getAddr(),
                        Const.ACCOUNT_TYPE);
                Bundle extras = new Bundle();
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                ContentResolver.requestSync(account, Const.AUTHORITY,
                        extras);
                return true;
            }
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETUP_WIZZARD) {
            switch (resultCode) {
                case RESULT_OK:
                    onCreateContinued();
                    return;
            }
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MailAcc acc = FastSyncAccess.getMailAccSequence(this).getCurrent();
        if (acc == null) {
            Const.logd(TAG + ": start SetupWizzard");
            startActivityForResult(
                    new Intent().setClass(this, SetupWizzard.class),
                    REQUEST_CODE_SETUP_WIZZARD);
            return;
        }

        onCreateContinued();
    }

    private void onCreateContinued() {
        setContentView(R.layout.activity_main);
        bindViews();

        onAction(R.id.actionRequestSync, null);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new LeftmostFragment();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.vLeftmost, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }
}
