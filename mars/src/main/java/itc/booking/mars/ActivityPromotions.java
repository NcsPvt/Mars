package itc.booking.mars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import itcurves.mars.R;

public class ActivityPromotions extends Activity implements CallbackResponseListener {

    public class PromotionAdapter extends ArrayAdapter<Promotion> {

        private final ArrayList<Promotion> promosList;
        private final int viewResourceId;
        private Context myContext;

        public PromotionAdapter(Context context, int _viewResourceId, ArrayList<Promotion> promoList) {

            super(context, _viewResourceId, promoList);
            myContext = context;
            viewResourceId = _viewResourceId;
            promosList = promoList;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        /**
         * Returns the position of the specified item in the array.
         *
         * @param item The item to retrieve the position of.
         * @return The position of the specified item, matching the Lat Long.
         * @author Muhammad Zahid
         */
        @Override
        public int getPosition(Promotion item) {
            for (int i = 0; i < promosList.size(); i++)
                if (promosList.get(i).promoDescription.equalsIgnoreCase(item.promoDescription))
                    return i;
            return -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final Promotion currentPromotion = promosList.get(position);
            View currentView = convertView;

            if (currentView == null) {
                LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = vi.inflate(viewResourceId, null);
            }
            if (currentPromotion != null) {

                ImageView companyLogo = (ImageView) currentView.findViewById(R.id.promo_company_logo);
                TextView promoDesc = (TextView) currentView.findViewById(R.id.promo_desc);
                promoDesc.setText(currentPromotion.promoDescription);

                ImageView availPromo = (ImageView) currentView.findViewById(R.id.btn_avail_promo);
                if (currentPromotion.promoCode.length() > 0) {
                    if (currentPromotion.promoCode.equalsIgnoreCase("share")) {
                        availPromo.setImageResource(R.drawable.invite);
                        availPromo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BookingApplication.share_App();
                            }
                        });
                    } else {
                        availPromo.setImageResource(R.drawable.collect);
                        availPromo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BookingApplication.AvailPromotion(currentPromotion.promoCode, Integer.toString(currentPromotion.companyID), ActivityPromotions.this);
                            }
                        });
                    }
                    availPromo.setVisibility(View.VISIBLE);
                } else
                    availPromo.setVisibility(View.INVISIBLE);

                if (currentPromotion.companyLogoLink.length() > 0)
                    BookingApplication.imagedownloader.DisplayImage(currentPromotion.companyLogoLink, companyLogo);

            }
            return currentView;
        }
    } // PromotionAdapter Class

    public class PromotionDetailAdapter extends ArrayAdapter<BookingApplication.Campaign> {

        private final ArrayList<BookingApplication.Campaign> promosDetailList;
        private final int viewResourceId;
        private Context myContext;
        private int selectedIndex = -1;


        public void setSelectedIndex(int ind) {
            selectedIndex = ind;
        }
        public PromotionDetailAdapter(Context context, int _viewResourceId, ArrayList<BookingApplication.Campaign> promoList) {

            super(context, _viewResourceId, promoList);
            myContext = context;
            viewResourceId = _viewResourceId;
            promosDetailList = promoList;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        /**
         * Returns the position of the specified item in the array.
         *
         * @param item The item to retrieve the position of.
         * @return The position of the specified item, matching the Lat Long.
         * @author Muhammad Zahid
         */
        @Override
        public int getPosition(BookingApplication.Campaign item) {
            for (int i = 0; i < promosDetailList.size(); i++)
                if (promosDetailList.get(i).PromoCode.equalsIgnoreCase(item.PromoCode))
                    return i;
            return -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final BookingApplication.Campaign currentPromotion = promosDetailList.get(position);
            View currentView = convertView;

            if (currentView == null) {
                LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = vi.inflate(viewResourceId, null);
            }

            if (position == selectedIndex) {
                currentView.setBackgroundColor(getResources().getColor(R.color.mars_cyan));
            } else {
                currentView.setBackgroundColor(getResources().getColor(R.color.white));
            }

            if (currentPromotion != null) {

                ImageView companyLogo = (ImageView) currentView.findViewById(R.id.iv_promo_type);
                TextView tv_promo_type = (TextView) currentView.findViewById(R.id.tv_promo_type);
                TextView tv_PromotionCode = (TextView) currentView.findViewById(R.id.tv_PromotionCode);
                TextView tv_promo_balance = (TextView) currentView.findViewById(R.id.tv_promo_balance);
                TextView tv_promo_expiry = (TextView) currentView.findViewById(R.id.tv_promo_expiry);

                tv_promo_type.setText(currentPromotion.CampaignName);
                tv_PromotionCode.setText(currentPromotion.PromoCode);
                tv_promo_balance.setText(currentPromotion.Balance);
                tv_promo_expiry.setText(currentPromotion.dtExpiry);

                if (currentPromotion.PromoURL.length() > 0)
                    BookingApplication.imagedownloader.DisplayImage(currentPromotion.PromoURL, companyLogo);

            }
            return currentView;
        }
    } // PromotionDetailAdapter Class

    public class InviteeAdapter extends ArrayAdapter<String> {

        private final ArrayList<String> inviteeList;
        private final int viewResourceId;
        private Context myContext;

        public InviteeAdapter(Context context, int _viewResourceId, ArrayList<String> _inviteeList) {

            super(context, _viewResourceId, _inviteeList);
            myContext = context;
            viewResourceId = _viewResourceId;
            inviteeList = _inviteeList;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        /**
         * Returns the position of the specified item in the array.
         *
         * @param item The item to retrieve the position of.
         * @return The position of the specified item, matching the Lat Long.
         * @author Muhammad Zahid
         */
        @Override
        public int getPosition(String item) {
            for (int i = 0; i < inviteeList.size(); i++)
                if (inviteeList.get(i).equalsIgnoreCase(item))
                    return i;
            return -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final String currentInvitee = inviteeList.get(position);
            View currentView = convertView;

            if (currentView == null) {
                LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = vi.inflate(viewResourceId, null);
            }
            if (currentInvitee != null) {

                TextView promoMsg = (TextView) currentView.findViewById(R.id.tv_partner_promo_text);
                promoMsg.setText(currentInvitee);

                ImageView companyLogo = (ImageView) currentView.findViewById(R.id.iv_promo_company_logo);
                companyLogo.setImageResource(R.drawable.ic_driver);

            }
            return currentView;
        }
    } // AffiliatePromoAdapter Class

    private int selectedPromo = -1;
    private ListView promosListView, list_ready_promos, list_affiliates_promo;
    private PromotionAdapter promo_adapter;
    private PromotionDetailAdapter promo_detail_adapter;
    private InviteeAdapter invitee_adapter;
    private LinearLayout ll_pageHeader, ll_getting_promos_progress;
    private RelativeLayout rl_main_promotions, rl_promotion_details;
    private ImageView iv_company_logo;
    private TextView rewardPoints, totalTrips, tv_selected_code, tv_selected_balance, tv_selected_perTrip, tv_no_promos_available, promosReadyHeader, rewardsHeader, inviteesHeader;

    @Override
    /*------------------------------------------------- onCreate -----------------------------------------------------------------------*/
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        BookingApplication.setMyTheme(ActivityPromotions.this);
        setContentView(R.layout.activity_promotions);

        ll_getting_promos_progress = (LinearLayout) findViewById(R.id.ll_getting_promos_progress);
        ll_pageHeader = (LinearLayout) findViewById(R.id.ll_pageHeader);
        ll_pageHeader.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
        rl_main_promotions = (RelativeLayout) findViewById(R.id.rl_main_promotions);
        iv_company_logo = (ImageView) findViewById(R.id.iv_company_logo);
        tv_no_promos_available = (TextView) findViewById(R.id.tv_no_promos_available);
        rewardPoints = (TextView) findViewById(R.id.rewardPoints);
        totalTrips = (TextView) findViewById(R.id.totalTrips);
        tv_selected_code = (TextView) findViewById(R.id.tv_selected_code);
        tv_selected_balance = (TextView) findViewById(R.id.tv_selected_balance);
        tv_selected_perTrip = (TextView) findViewById(R.id.tv_selected_perTrip);
        rl_promotion_details = (RelativeLayout) findViewById(R.id.rl_promotion_details);
        //rl_promotion_details.setBackgroundResource(BookingApplication.textView_Background);
        promosReadyHeader = (TextView) findViewById(R.id.promosReadyHeader);
        promosReadyHeader.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
        rewardsHeader = (TextView) findViewById(R.id.rewardsHeader);
        rewardsHeader.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
        inviteesHeader = (TextView) findViewById(R.id.inviteesHeader);
        inviteesHeader.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));

        list_ready_promos = (ListView) findViewById(R.id.list_ready_promos);
        list_affiliates_promo = (ListView) findViewById(R.id.list_affiliates_promo);

        TextView no_promo_available = (TextView) findViewById(R.id.no_promo_available);
        if (BookingApplication.promotions.size() > 0)
            no_promo_available.setVisibility(View.GONE);

        promosListView = (ListView) findViewById(R.id.list_promos);
        promo_adapter = new PromotionAdapter(this, R.layout.list_item_promo, BookingApplication.promotions);
        promosListView.setAdapter(promo_adapter);

        promo_detail_adapter = new PromotionDetailAdapter(this, R.layout.list_item_promo_detail, BookingApplication.activePromotions);
        list_ready_promos.setAdapter(promo_detail_adapter);
        list_ready_promos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookingApplication.Campaign camp = BookingApplication.activePromotions.get(position);
                tv_selected_code.setText(camp.PromoCode);
                tv_selected_balance.setText(camp.Balance);
                tv_selected_perTrip.setText(camp.PerTripAmount);

                selectedPromo = position;
                promo_detail_adapter.setSelectedIndex(selectedPromo);
                promo_detail_adapter.notifyDataSetChanged();
            }
        });

        invitee_adapter = new InviteeAdapter(this, R.layout.list_item_promo_partner, BookingApplication.invitees);
        list_affiliates_promo.setAdapter(invitee_adapter);

    }

    /*------------------------------------------------- onResume -------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.callerContext = this;
        ll_getting_promos_progress.setVisibility(View.VISIBLE);
        BookingApplication.getNearbyVehicles(BookingApplication.currentAddress, null, false);
    }

    /*------------------------------------------------- showMyPromos --------------------------------------------------------------------------------------*/
    public void showMyPromos(View v) {
        rl_promotion_details.setVisibility(View.VISIBLE);
        rl_main_promotions.setVisibility(View.GONE);
        String bottomLogo = BookingApplication.userInfoPrefs.getString("BottomLogoImage", "");
        if ((bottomLogo.length() > 0) && (bottomLogo.endsWith("png") || bottomLogo.endsWith("jpg")))
            BookingApplication.imagedownloader.DisplayImage(bottomLogo, iv_company_logo);
        BookingApplication.getUserPromoDetail(BookingApplication.CompanyID);
    }

    /*------------------------------------------------- showTermsOfUse --------------------------------------------------------------------------------------*/
    public void showTermsOfUse(View v) {
        if (selectedPromo > -1)
            showCustomDialog(R.string.promotions, BookingApplication.activePromotions.get(selectedPromo).termsOfUse, 0, false);
    }


    /*------------------------------------------------- onBackPressed --------------------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        if (rl_main_promotions.isShown())
            ActivityPromotions.this.finish();
        else {
            rl_promotion_details.setVisibility(View.GONE);
            rl_main_promotions.setVisibility(View.VISIBLE);
        }

    }

    /*------------------------------------------------ Custom  Dialog -------------------------------------------------------------------------------------*/
    public void showCustomDialog(int dialogTitle, final String dialogText, int imageResID, final Boolean showCancelBtn) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);

        final AlertDialog thisDialog = adb.create();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_layout, null);

        TextView title_text = (TextView) layout.findViewById(R.id.dialog_title);
        TextView text = (TextView) layout.findViewById(R.id.dialog_msg);
        if (imageResID > 0)
            title_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, imageResID, 0);
        title_text.setText(dialogTitle);
        text.setText(dialogText);
        Button btn_OK = (Button) layout.findViewById(R.id.btnYES);
        Button btn_CANCEL = (Button) layout.findViewById(R.id.btnNo);
        if (!showCancelBtn)
            btn_CANCEL.setVisibility(View.GONE);
        btn_OK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                thisDialog.dismiss();
                showMyPromos(null);
            }
        });

        btn_CANCEL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                thisDialog.dismiss();
            }
        });

        thisDialog.setView(layout);
        if (dialogText.length() > 0)
            thisDialog.show();
    }


    /*-------------------------------------------- callbackResponseReceived --------------------------------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean success) {
        try {
            switch (apiCalled) {
                case BookingApplication.APIs.GETUSERPROMOTIONDETAIL:
                    if (success) {

                        if (BookingApplication.activePromotions.size() > 0)
                            tv_no_promos_available.setVisibility(View.GONE);
                        else
                            tv_no_promos_available.setVisibility(View.VISIBLE);

                        promo_detail_adapter.notifyDataSetChanged();
                        invitee_adapter.notifyDataSetChanged();
                        rewardPoints.setText(getResources().getString(R.string.Points, jsonResponse.getString("RewardPoints")));
                        totalTrips.setText(getResources().getString(R.string.ExpiryPoints, jsonResponse.getString("TotalTrips")));
                    } else {
                        rewardPoints.setText(getResources().getString(R.string.Points, "0"));
                        totalTrips.setText(getResources().getString(R.string.ExpiryPoints, "0"));
                    }
                    break;
                case BookingApplication.APIs.AvailPromotion:
                    if (success) {
                        showCustomDialog(R.string.promotions, jsonResponse.getString("responseMessage"), 0, false);
                    }
                    break;
                case BookingApplication.APIs.GETNEARBYVEHICLES:

                    ll_getting_promos_progress.setVisibility(View.GONE);
                    promo_adapter.notifyDataSetChanged();
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(ActivityPromotions.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

}