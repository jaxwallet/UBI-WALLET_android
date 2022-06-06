package com.bonuswallet.app.interact;

import static com.bonuswallet.app.C.ETHER_DECIMALS;
import static com.bonuswallet.app.entity.tokens.Token.TOKEN_BALANCE_PRECISION;

import android.util.Log;

import com.bonuswallet.app.BuildConfig;
import com.bonuswallet.app.entity.CustomViewSettings;
import com.bonuswallet.app.entity.Wallet;
import com.bonuswallet.app.repository.WalletRepositoryType;
import com.bonuswallet.app.util.BalanceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GenericWalletInteract
{
	private final WalletRepositoryType walletRepository;

	public GenericWalletInteract(WalletRepositoryType walletRepository) {
		this.walletRepository = walletRepository;
	}

	public Single<Wallet> find() {
		return walletRepository.getDefaultWallet()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}

	/**
	 * Called when wallet marked as backed up.
	 * Update the wallet date
	 *
	 * @param walletAddr
	 */
	public void updateBackupTime(String walletAddr)
	{
		walletRepository.updateBackupTime(walletAddr);
	}

	/**
	 * Called when wallet has had its backup warning dismissed
	 * Update the wallet date
	 *
	 * @param walletAddr
	 */
	public void updateWarningTime(String walletAddr)
	{
		walletRepository.updateWarningTime(walletAddr);
	}

	public Single<String> getWalletNeedsBackup(String walletAddr)
	{
		return walletRepository.getWalletRequiresBackup(walletAddr);
	}

	public void setIsDismissed(String walletAddr, boolean isDismissed)
	{
		walletRepository.setIsDismissed(walletAddr, isDismissed);
	}

	/**
	 * Check if a wallet needs to be backed up.
	 * @param walletAddr
	 * @return
	 */
	public Single<Boolean> getBackupWarning(String walletAddr)
	{
		return walletRepository.getWalletBackupWarning(walletAddr);
	}

	/**
	 * Check if a wallet needs to verify kyc.
	 * @param walletAddr
	 * @return
	 */
	public Single<String> getKYCStatus(String walletAddr)
	{
		return walletRepository.getKYCStatus(walletAddr);
	}

	public void updateWalletInfo(Wallet wallet, String name, Realm.Transaction.OnSuccess onSuccess)
	{
		wallet.name = name;
		walletRepository.updateWalletData(wallet, onSuccess);
	}

	public void updateBalanceIfRequired(Wallet wallet, BigDecimal newBalance)
	{
		String newBalanceStr = BalanceUtils.getScaledValueFixed(newBalance, ETHER_DECIMALS, TOKEN_BALANCE_PRECISION);
		if (!newBalance.equals(BigDecimal.valueOf(-1)) && !wallet.balance.equals(newBalanceStr))
		{
			wallet.balance = newBalanceStr;
			walletRepository.updateWalletData(wallet, () -> {
				if (BuildConfig.DEBUG) Log.d(getClass().getCanonicalName(), "Updated balance");
			});
		}
	}

	public Realm getWalletRealm()
	{
		return walletRepository.getWalletRealm();
	}

    public enum BackupLevel
	{
		BACKUP_NOT_REQUIRED, WALLET_HAS_LOW_VALUE, WALLET_HAS_HIGH_VALUE
	}
	public enum KycStatus
	{
		INIT, APPROVED, REJECTED, PENDING
	}
}
