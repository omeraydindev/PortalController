package ma.portal.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.portal.controller.R;

public class HostsRVAdapter extends RecyclerView.Adapter<HostsRVAdapter.ViewHolder> {
    private final List<String> hosts;
    private final LayoutInflater layoutInflater;
    private HostClickListener hostClickListener;

    public HostsRVAdapter(Context context, List<String> hosts) {
        layoutInflater = LayoutInflater.from(context);
        this.hosts = hosts;
    }

    public void setHostClickListener(HostClickListener hostClickListener) {
        this.hostClickListener = hostClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.host_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txHost.setText(hosts.get(position));
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    public interface HostClickListener {
        void onClick(String host);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView txHost;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            txHost = itemView.findViewById(R.id.tx_token);
        }

        @Override
        public void onClick(View v) {
            hostClickListener.onClick(hosts.get(getAdapterPosition()));
        }
    }
}
