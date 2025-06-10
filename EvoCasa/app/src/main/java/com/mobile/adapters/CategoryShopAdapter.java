package com.mobile.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.Category;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import java.util.List;

public class CategoryShopAdapter extends RecyclerView.Adapter<CategoryShopAdapter.CategoryViewHolder> {

    private final List<Category> categoryList;
    private final Context context;

    public CategoryShopAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @Override
    public int getItemViewType(int position) {
        // Item cuối cùng dùng layout khác, các item khác dùng layout thường
        return position == categoryList.size() - 1 ? 1 : 0;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout khác nhau cho item cuối và các item thường
        int layoutId = viewType == 1 ? R.layout.item_category_shop_last : R.layout.item_category_shop;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        // Tính chiều rộng mỗi item theo số cột (2 cột)
        int screenWidth = parent.getResources().getDisplayMetrics().widthPixels;
        int spacingPx = (int) (17 * 2 * parent.getResources().getDisplayMetrics().density); // spacing giữa các item
        int paddingPx = (int) (16 * 2 * parent.getResources().getDisplayMetrics().density); // padding của RecyclerView
        int itemWidth = (screenWidth - spacingPx - paddingPx) / 2;

        // Đặt chiều cao cố định cho hình chữ nhật (ví dụ: 3/4 chiều rộng)
        int itemHeight = (int) (itemWidth * 0.75f);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(itemWidth, itemHeight);
        view.setLayoutParams(layoutParams);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        // Thiết lập font Zbold cho TextView
        FontUtils.setZboldFont(context, holder.txtCategoryName);

        // Căn giữa text cho tất cả item
        holder.txtCategoryName.setGravity(Gravity.CENTER);

        // Format text để xuống dòng nếu có 2 từ trở lên (trừ item cuối)
        String categoryName = category.getName();
        if (position != categoryList.size() - 1) { // Không phải item cuối
            categoryName = formatCategoryText(categoryName);
        }

        holder.txtCategoryName.setText(categoryName);
        holder.imgCategory.setImageResource(category.getImageResId());

        // Xử lý item cuối cùng chiếm 2 cột (chỉ cần xử lý size, layout đã khác rồi)
        if (position == categoryList.size() - 1) {
            int screenWidth = holder.itemView.getResources().getDisplayMetrics().widthPixels;
            int paddingPx = (int) (17 * 2 * holder.itemView.getResources().getDisplayMetrics().density);
            int spacingPx = (int) (8 * holder.itemView.getResources().getDisplayMetrics().density);

            int fullWidth = screenWidth - paddingPx - spacingPx;

            // Giảm chiều rộng ví dụ 32dp (theo density)
            int reduceWidthPx = (int) (14.5 * holder.itemView.getResources().getDisplayMetrics().density);
            int newWidth = fullWidth - reduceWidthPx;

            int itemHeight = (int) (newWidth * 0.75f / 2);

            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(newWidth, itemHeight);
            // Đặt margin 2 bên đều, mỗi bên giảm một nửa reduceWidthPx
            int marginSide = reduceWidthPx / 2;
            layoutParams.setMargins(marginSide, 0, marginSide, 0);

            holder.itemView.setLayoutParams(layoutParams);
        }
    }

    // Method để format text xuống dòng (không cần căn giữa bằng space nữa)
    private String formatCategoryText(String text) {
        if (text.contains(" ")) {
            String[] words = text.split(" ");
            if (words.length == 2) {
                return words[0] + "\n" + words[1];
            } else if (words.length > 2) {
                // Xử lý trường hợp có nhiều hơn 2 từ như "Dining & Entertaining"
                StringBuilder firstLine = new StringBuilder();
                StringBuilder secondLine = new StringBuilder();

                // Chia đôi các từ
                int mid = words.length / 2;
                for (int i = 0; i < mid; i++) {
                    if (i > 0) firstLine.append(" ");
                    firstLine.append(words[i]);
                }
                for (int i = mid; i < words.length; i++) {
                    if (i > mid) secondLine.append(" ");
                    secondLine.append(words[i]);
                }

                return firstLine + "\n" + secondLine;
            } else {
                // Fallback: xuống dòng sau từ đầu tiên
                StringBuilder formattedName = new StringBuilder();
                formattedName.append(words[0]).append("\n");
                for (int i = 1; i < words.length; i++) {
                    if (i > 1) formattedName.append(" ");
                    formattedName.append(words[i]);
                }
                return formattedName.toString();
            }
        }
        return text;
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName;
        ImageView imgCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            imgCategory = itemView.findViewById(R.id.imgCategory);
        }
    }
}