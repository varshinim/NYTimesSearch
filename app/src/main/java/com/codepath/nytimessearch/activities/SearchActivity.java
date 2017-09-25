package com.codepath.nytimessearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.nytimessearch.FilterDialog;
import com.codepath.nytimessearch.InfiniteScrollListener;
import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.adapter.ArticleArrayAdapter;
import com.codepath.nytimessearch.model.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import cz.msebera.android.httpclient.Header;

import static android.R.attr.offset;

public class SearchActivity extends AppCompatActivity implements FilterDialog.FilterDialogListener {

    private InfiniteScrollListener scrollListener;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;
    RecyclerView listArticles;
    StaggeredGridLayoutManager gridLayoutManager;

    String query;
    Date mDate;
    String mSpinner;
    Boolean mArtChecked;
    Boolean mFashionChecked;
    Boolean mSportChecked;

    int hitCount = 0;

    public String formatDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(date);
        Log.d("Formatted date: ", dateStr);
        return dateStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new InfiniteScrollListener(gridLayoutManager) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                Log.d("onLoadMore: ", "page = "+page);
                Log.d("onLoadMore: ", "totalItemsCount = "+totalItemsCount);
                if (hitCount == 0 || totalItemsCount < hitCount) {
                    loadNextDataFromApi(page);
                }
                return true;
            }
        };
        // Adds the scroll listener to RecyclerView
        listArticles.addOnScrollListener(scrollListener);
    }

    public void setupViews(){
        articles = new ArrayList<>();

        listArticles = (RecyclerView) findViewById(R.id.rvArticles);
        //hook up listener for grid click
        ArticleArrayAdapter.RecyclerViewClickListener listener = new ArticleArrayAdapter.RecyclerViewClickListener(){
            @Override
            public void recyclerViewListClicked(View v, int position) {
                // create an intent to display the article
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                // get the article to diasplay
                Article article = articles.get(position);
                // pass that article into intent
                i.putExtra("article", Parcels.wrap(article));
                // launch the activity
                startActivity(i);
            }
        };

        adapter = new ArticleArrayAdapter(this, articles, listener);
        // Attach the adapter to the recyclerview to populate items
        listArticles.setAdapter(adapter);
        // Set StaggeredGridlayout manager to position the items
        gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // Attach the layout manager to the recycler view
        listArticles.setLayoutManager(gridLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newQuery) {
                // performing query
                query = newQuery;
                callNYTimes(query, 0);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            FragmentManager manager = getSupportFragmentManager();
            FilterDialog dialog = new FilterDialog();

            if (mDate != null) {
                Bundle filterData = new Bundle();
                filterData.putString("date", formatDate(mDate));
                filterData.putString("sort", mSpinner);
                filterData.putBoolean("art", mArtChecked);
                filterData.putBoolean("fashion", mFashionChecked);
                filterData.putBoolean("sport", mSportChecked);
                dialog.setArguments(filterData);
            }
            dialog.show(manager, "Filtered");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinishEditDialog(Date date, String spinner, Boolean art, Boolean fashion, Boolean sport) {
        mDate = date;
        mSpinner = spinner;
        mArtChecked = art;
        mFashionChecked = fashion;
        mSportChecked = sport;
    }


    public void loadNextDataFromApi(int offset) {
        Log.d("loadNextDataFromApi:" ," OFFSET = "+ offset);
        callNYTimes(query, offset);
        adapter.notifyDataSetChanged();
    }

    public void callNYTimes(String query, int offset) {
        AsyncHttpClient client = new AsyncHttpClient();
        String strUrl = getString(R.string.NYT_SEARCH_API);
        String apiKey = getString(R.string.NYT_SEARCH_API_KEY);

        RequestParams params = new RequestParams();
        params.put("api-key", apiKey);
        params.put("page", offset);
        params.put("q", query);

        String fq = "";
        if (mArtChecked || mFashionChecked || mSportChecked) {
            fq += " news_desk: (";
            if (mArtChecked) {
                fq += "\"Arts\"";
            }
            if (mFashionChecked) {
                fq += "\"Fashion & Style\"";
            }
            if (mSportChecked) {
                fq += "\"Sports\"";
            }
            fq += ")";
        }

        params.put("fq",fq);
        if (mSpinner != null) {
            params.put("sort", mSpinner);
        }

        if (mDate != null) {
            params.put("begin_date", formatDate(mDate));
        }

        client.get(strUrl, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                Log.d("DEBUG",response.toString());
                JSONArray articleJsonResults = null;
                try{
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    Log.d("callNYTimes:", articleJsonResults.toString());
                    articles.addAll(Article.fromJSONArray(articleJsonResults));
                    // making changes directly to adapter modifies the underline data and add it to array list
                    adapter.notifyDataSetChanged();
                    Log.d("callNYTimes:", "Number of articles recieved = " + articles.size());
                    hitCount = response.getJSONObject("response").getJSONObject("meta").getInt("hits");
                    Log.d("Hit Count:", ""+hitCount);
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject json) {
                Log.d("onFailure: ", "" + statusCode);
                Log.d("onFailure: ", "" + json);
            }
        });
    }

}
