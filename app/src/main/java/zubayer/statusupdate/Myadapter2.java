package zubayer.statusupdate;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class Myadapter2 extends ArrayAdapter<String> {

    public Typeface font;
    ArrayList<String> names;
    ArrayList<String> dates;
    ArrayList<String> text;
    private Activity context;
    Spinner spinner;

    public Myadapter2(Activity context, ArrayList<String> names,ArrayList<String> dates,ArrayList<String> text) {

        super(context, R.layout.cardview, names);
        this.context=context;
        this.names=names;
        this.dates=dates;
        this.text=text;

        font= Typeface.createFromAsset(context.getAssets(),"kalpurush.ttf");
    }
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View row=inflater.inflate(R.layout.cardview,null);
        TextView nam = row.findViewById(R.id.name);
        TextView dat = row.findViewById(R.id.date);
        TextView delv = row.findViewById(R.id.delv);
        nam.setTypeface(font);
        dat.setTypeface(font);
        nam.setText(names.get(position));
        dat.setText(dates.get(position));
        delv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                names.remove(position);
                dates.remove(position);
                text.remove(position);
                notifyDataSetChanged();

            }
        });

        return  row;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
