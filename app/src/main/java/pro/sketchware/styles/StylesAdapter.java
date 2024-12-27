package pro.sketchware.styles;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pro.sketchware.databinding.ItemStyleBinding;

import java.util.ArrayList;
import java.util.List;

public class StylesAdapter extends RecyclerView.Adapter<StylesAdapter.StyleViewHolder> {

    private final List<StyleModel> stylesList;
    private final List<StyleModel> originalList;
    private final StylesActivity activity;

    public StylesAdapter(ArrayList<StyleModel> stylesList, StylesActivity activity) {
        this.stylesList = stylesList;
        this.originalList = new ArrayList<>(stylesList);
        this.activity = activity;
    }

    @NonNull
    @Override
    public StyleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStyleBinding binding = ItemStyleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StyleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StyleViewHolder holder, int position) {
        StyleModel style = stylesList.get(position);
        holder.bind(style);
    }

    @Override
    public int getItemCount() {
        return stylesList.size();
    }

    public void filter(String newText) {
        stylesList.clear();

        for (StyleModel style : originalList) {
            if (style.getStyleName().toLowerCase().contains(newText)) {
                stylesList.add(style);
            }
        }

        notifyDataSetChanged();
    }

    public class StyleViewHolder extends RecyclerView.ViewHolder {

        private final ItemStyleBinding binding;

        public StyleViewHolder(ItemStyleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(StyleModel style) {
            binding.styleName.setText(style.getStyleName());
            if (style.getParent().isEmpty()) {
                binding.styleParent.setText("No Parent");
            } else {
                binding.styleParent.setText(style.getParent());
            }

            binding.getRoot().setOnClickListener(view -> activity.showStyleAttributesDialog(getAbsoluteAdapterPosition()));

            binding.getRoot().setOnLongClickListener(view -> {
                activity.showEditStyleDialog(getAbsoluteAdapterPosition());
                return true;
            });
        }
    }
}