package david.com.popularmovies.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import david.com.popularmovies.R;

/**
 * Created by David on 13-Jun-17.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHashMap;
    private static final String TAG = ExpandableListAdapter.class.getSimpleName();

    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listHashMap) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;

        //Log.d(TAG, Arrays.toString(listHashMap.get(0).toArray()));

        Log.d(TAG, "******* in constructor, listDataHeader size is: " + this.listDataHeader.size() + " & includes: " + this.listDataHeader.get(0) + "  ....and listHashMap size is " + this.listHashMap.get(listDataHeader.get(0)).size() + "...and includes " + Arrays.toString(this.listHashMap.get(listDataHeader.get(0)).toArray()));
    }

    @Override
    public int getGroupCount() {
        Log.d(TAG, "in getGroupCount & count is: " + listDataHeader.size());
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Log.d(TAG, "in getChildrenCount & position is: " + groupPosition);
        Log.d(TAG, "getChildrenCount, size of trailer list is: ---" + listHashMap.get(listDataHeader.get(groupPosition)).size());
        return listHashMap.get(listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        Log.d(TAG, "in getGroup & groupPosition is: " + groupPosition);
        return listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Log.d(TAG, "in getChild & groupPosition is: " + groupPosition + ", and child position is: " + childPosition);
        return listHashMap.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        Log.d(TAG, "in getGroupId & groupPosition is: " + groupPosition);
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Log.d(TAG, "in getChildId & groupPosition is: " + groupPosition + ", and child position is: " + childPosition);
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.d(TAG, "in getGroupView returning VIEW");
        String headerTitle = (String) getGroup(groupPosition);
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_trailer_group, null);
        }
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Log.d(TAG, " ???????? in getChildView returning VIEW");
//      final String childText = (String) getChild(groupPosition, childPosition);
        final String childText = (String) (String.valueOf(childPosition + 1));
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_trailer_item, null);
        }
        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play, 0, 0, 0);
        txtListChild.setText("Trailer " + childText);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
