package gh.sammie.myshoppinglist.Holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import gh.sammie.myshoppinglist.Interface.ItemClickListener;
import gh.sammie.myshoppinglist.R;

public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private View myView;
    private ItemClickListener itemClickListener;

    public MyViewHolder(View itemView) {
        super(itemView);
        myView = itemView;

        myView.setOnClickListener(this);
    }


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setType(String type) {
        TextView mType = myView.findViewById(R.id.type);
        mType.setText(type);
    }

    public void setNote(String note) {
        TextView mNote = myView.findViewById(R.id.note);
        mNote.setText(note);
    }

    public void setDate(String date) {
        TextView mDate = myView.findViewById(R.id.date);
        mDate.setText(date);
    }

    public void setAmmount(int ammount) {

        TextView mAmount = myView.findViewById(R.id.amount);
        String stam = String.valueOf(ammount);
        mAmount.setText("GH¢ "+stam);

    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
