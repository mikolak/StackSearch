package com.kojdecki.stacksearch.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kojdecki.stacksearch.R;
import com.kojdecki.stacksearch.retrofit.Question;
import com.kojdecki.stacksearch.retrofit.ShallowUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by calot on 2/16/16.
 */
public class QuestionListAdapter extends BaseAdapter {
    private static final String TAG = "QuestionListAdapter";
    private ArrayList<Question> items = null;
    private Context context = null;

    public QuestionListAdapter(ArrayList<Question> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Question getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) convertView.findViewById(R.id.item_image);
            viewHolder.title = (TextView) convertView.findViewById(R.id.item_title);
            viewHolder.details = (TextView) convertView.findViewById(R.id.item_details);
            viewHolder.answerCount = (TextView) convertView.findViewById(R.id.answer_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        class CropCircleTransformation implements Transformation {

            @Override
            public Bitmap transform(Bitmap source) {
                int diameter = Math.min(source.getWidth(), source.getHeight());
                int x_c = source.getWidth()/2;
                int y_c = source.getHeight()/2;
                int x = (source.getWidth() - diameter)/2;
                int y = (source.getHeight() - diameter)/2;
                Bitmap cropped = Bitmap.createBitmap(source, x, y, diameter, diameter);
                Bitmap result = cropped.copy(cropped.getConfig(), true);
                cropped.recycle();
                source.recycle();
                result.setHasAlpha(true);
                for (x = 0; x < diameter; x++)
                    for (y = 0; y < diameter; y++) {
                        if ((x - x_c) * (x - x_c) + (y - y_c) * (y - y_c) > (diameter/2) * (diameter/2))
                            result.setPixel(x, y, Color.parseColor("#00000000"));
                    }
                return result;
            }

            @Override
            public String key() {
                return "circle()";
            }
        }
        //Custom view extending ImageView just to override onDraw and set alpha on the bitmap outside the
        //circle seems to be an overkill to me
        if (getItem(position).getOwner().getProfile_image() != null)
            Picasso.with(context)
                .load(getItem(position).getOwner().getProfile_image())
                .transform(new CropCircleTransformation())
                .resize(128, 128)
                .into(viewHolder.image);
        viewHolder.title.setText(getItem(position).getTitle());
        viewHolder.details.setText(getItem(position).getOwner().getDisplay_name());
        if (getItem(position).getAnswer_count() >= 0)
            viewHolder.answerCount.setText(String.valueOf(getItem(position).getAnswer_count()));

        return convertView;
    }

    public void add(Question object) {
        items.add(object);
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends Question> collection) {
        items.addAll(collection);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public ArrayList<Question> getItems() {
        return items;
    }

    public void setItems(ArrayList<Question> items) {
        this.items = items;
    }

    private class ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView details;
        public TextView answerCount;
    }
}
