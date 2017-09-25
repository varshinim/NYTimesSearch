package com.codepath.nytimessearch.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.model.Article;

import java.util.List;


public class ArticleArrayAdapter extends RecyclerView.Adapter<ArticleArrayAdapter.ViewHolder>{


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        public ImageView articleImage;
        public TextView articleTitle;
        public TextView articleSnippet;

        public ViewHolder(View article) {
            super(article);
            articleImage = (ImageView) article.findViewById(R.id.ivArticleImage);
            articleTitle = (TextView) article.findViewById(R.id.tvTitle);
            articleSnippet = (TextView) article.findViewById(R.id.tvSnippet);
            article.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());

        }
    }

    public interface RecyclerViewClickListener {
        public void recyclerViewListClicked(View v, int position);
    }

    private List<Article> articleList;
    private Context context;
    private static RecyclerViewClickListener itemListener;

    public ArticleArrayAdapter(Context context, List<Article> articleList, RecyclerViewClickListener itemListener){
        this.articleList = articleList;
        this.context = context;
        this.itemListener = itemListener;
    }

    @Override
    public ArticleArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View articleView;
        ArticleArrayAdapter.ViewHolder viewHolder;
        articleView = inflater.inflate(R.layout.item_article_result, parent, false);
        viewHolder = new ArticleArrayAdapter.ViewHolder(articleView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ArticleArrayAdapter.ViewHolder viewHolder, int position){
        Article article = articleList.get(position);

        ImageView imageView = viewHolder.articleImage;
        imageView.setImageResource(0);
        TextView textView = viewHolder.articleTitle;
        textView.setText(article.getHeadline());
        TextView snippetTextView = viewHolder.articleSnippet;
        snippetTextView.setText(article.getSnippet());
        String thumbnail = article.getThumbNail();
        if(!TextUtils.isEmpty(thumbnail)){
            Glide.with(context).load(thumbnail).into(imageView);
        }
    }

    @Override
    public int getItemCount(){
        return articleList.size();
    }

}
