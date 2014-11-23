package com.mani.activitylogger.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mani.activitylogger.R;
import com.mani.activitylogger.app.ActivitiesLoggerApplication;
import com.mani.activitylogger.model.DetectedActivity;
import com.mani.activitylogger.model.UserActivity;
import com.mani.activitylogger.ui.AddressView;
import com.mani.activitylogger.util.DateTimeUtil;
import com.mani.activitylogger.util.FontProvider;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by maniselvaraj on 29/9/14.
 */
public class ActivitiesListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private LayoutInflater mInflater;
    List<UserActivity> tripsList;

    public ActivitiesListAdapter() {
        mInflater = LayoutInflater.from(ActivitiesLoggerApplication.getContext());
    }

    public void setTrips(List<UserActivity> data) {
        this.tripsList = data;
        //Update the list items
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tripsList == null ? 0 : tripsList.size();
    }

    @Override
    public Object getItem(int position) {
        return tripsList == null ? null : tripsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activities_logger_list_item, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.addressView = (AddressView) convertView.findViewById(R.id.tripAddress);
            holder.tripTime = (TextView) convertView.findViewById(R.id.tripTime);
            holder.tripActivityIcon = (ImageView) convertView.findViewById(R.id.tripTypeIcon);

            holder.tripTime.setTypeface(FontProvider.getNormalFont());
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserActivity userActivity = tripsList.get(position);
        String startAddress = userActivity.getStartLocation().getAddress();
        String endAddress = userActivity.getEndLocation().getAddress();
        startAddress = (startAddress == null) ? "Not Found" : startAddress;
        endAddress = (endAddress == null) ? "Not Found" : endAddress;

        holder.tripActivityIcon.setImageResource(getTripActivityIcon(userActivity.getActivity()));
        holder.addressView.setStartAddressText(startAddress);
        holder.addressView.setEndAddressText(endAddress);
        holder.tripTime.setText(DateTimeUtil.getTripDisplayTime(userActivity.getStartTime(), userActivity.getEndTime()));

        return convertView;
    }

    class ViewHolder {
        AddressView addressView;
        TextView tripTime;
        ImageView tripActivityIcon;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.sticky_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.headerText);
            holder.text.setTypeface(FontProvider.getNormalFont());
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as date for this trip.
        UserActivity userActivity = tripsList.get(position);
        holder.text.setText(userActivity.getHeaderTxt());
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return tripsList.get(position).getHeaderId();
    }

    class HeaderViewHolder {
        TextView text;
    }

    private int getTripActivityIcon(DetectedActivity detectedActivity) {
        if (detectedActivity == DetectedActivity.WALK) {
            return R.drawable.icon_walk;
        } else if (detectedActivity == DetectedActivity.BICYCLE) {
            return R.drawable.icon_bike;
        } else if (detectedActivity == DetectedActivity.VEHICLE) {
            return R.drawable.icon_drive;
        }

        return R.drawable.icon_unknown;
    }
}
