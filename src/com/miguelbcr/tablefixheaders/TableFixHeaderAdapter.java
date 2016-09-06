package com.miguelbcr.tablefixheaders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract  class TableFixHeaderAdapter<
        TFIRSTHEADER, VFIRSTHEADER extends View & TableFixHeaderAdapter.FirstHeaderBinder<TFIRSTHEADER>,
        THEADER, VHEADER extends View & TableFixHeaderAdapter.HeaderBinder<THEADER>,
        TBODY,
        VFIRSTBODY extends View & TableFixHeaderAdapter.FirstBodyBinder<TBODY>,
        VBODY extends View & TableFixHeaderAdapter.BodyBinder<TBODY>,
        VSECTION extends View & TableFixHeaderAdapter.SectionBinder<TBODY>
        > extends BaseTableAdapter {

    private TFIRSTHEADER firstHeader;
    private List<THEADER> header = new ArrayList<THEADER>();
    private List<TBODY> firstBody = new ArrayList<TBODY>();
    private List<TBODY> body = new ArrayList<TBODY>();
    private List<TBODY> section = new ArrayList<TBODY>();
    private ClickListener<TFIRSTHEADER, VFIRSTHEADER> clickListenerFirstHeader;
    private ClickListener<THEADER, VHEADER> clickListenerHeader;
    private ClickListener<TBODY, VFIRSTBODY> clickListenerFirstBody;
    private ClickListener<TBODY, VBODY> clickListenerBody;
    private ClickListener<TBODY, VSECTION> clickListenerSection;
    private LongClickListener<TFIRSTHEADER, VFIRSTHEADER> longClickListenerFirstHeader;
    private LongClickListener<THEADER, VHEADER> longClickListenerHeader;
    private LongClickListener<TBODY, VFIRSTBODY> longClickListenerFirstBody;
    private LongClickListener<TBODY, VBODY> longClickListenerBody;
    private LongClickListener<TBODY, VSECTION> longClickListenerSection;


    public interface ClickListener<T, V> {
        void onClickItem(T t, V v, int row, int column);
    }

    public interface LongClickListener<T, V> {
        void onLongClickItem(T t, V v, int row, int column);
    }

    public interface FirstHeaderBinder<T> {
        void bindFirstHeader(T item);
    }

    public interface HeaderBinder<T> {
        void bindHeader(T item, int column);
    }

    public interface FirstBodyBinder<T> {
        void bindFirstBody(T item, int row);
    }
    public interface BodyBinder<T> {
        void bindBody(T item, int row, int column);
    }
    public interface SectionBinder<T> {
        void bindSection(T item, int row, int column);
    }

    protected abstract VFIRSTHEADER inflateFirstHeader();
    protected abstract VHEADER inflateHeader();
    protected abstract VFIRSTBODY inflateFirstBody();
    protected abstract VBODY inflateBody();
    protected abstract VSECTION inflateSection();
    protected abstract List<Integer> getHeaderWidths();
    protected abstract int getHeaderHeight();
    protected abstract int getSectionHeight();
    protected abstract int getBodyHeight();
    protected abstract boolean isSection(List<TBODY> items, int row);

    public TableFixHeaderAdapter(Context context) {
    }

    public void setClickListenerFirstHeader(ClickListener<TFIRSTHEADER, VFIRSTHEADER> clickListenerFirstHeader) {
        this.clickListenerFirstHeader = clickListenerFirstHeader;
    }

    public void setClickListenerHeader(ClickListener<THEADER, VHEADER> clickListenerHeader) {
        this.clickListenerHeader = clickListenerHeader;
    }

    public void setClickListenerFirstBody(ClickListener<TBODY, VFIRSTBODY> clickListenerFirstBody) {
        this.clickListenerFirstBody = clickListenerFirstBody;
    }

    public void setClickListenerBody(ClickListener<TBODY, VBODY> clickListenerBody) {
        this.clickListenerBody = clickListenerBody;
    }

    public void setClickListenerSection(ClickListener<TBODY, VSECTION> clickListenerSection) {
        this.clickListenerSection = clickListenerSection;
    }

    public void setLongClickListenerFirstHeader(LongClickListener<TFIRSTHEADER, VFIRSTHEADER> longClickListenerFirstHeader) {
        this.longClickListenerFirstHeader = longClickListenerFirstHeader;
    }

    public void setLongClickListenerHeader(LongClickListener<THEADER, VHEADER> longClickListenerHeader) {
        this.longClickListenerHeader = longClickListenerHeader;
    }

    public void setLongClickListenerFirstBody(LongClickListener<TBODY, VFIRSTBODY> longClickListenerFirstBody) {
        this.longClickListenerFirstBody = longClickListenerFirstBody;
    }

    public void setLongClickListenerBody(LongClickListener<TBODY, VBODY> longClickListenerBody) {
        this.longClickListenerBody = longClickListenerBody;
    }

    public void setLongClickListenerSection(LongClickListener<TBODY, VSECTION> longClickListenerSection) {
        this.longClickListenerSection = longClickListenerSection;
    }

    public void setFirstHeader(TFIRSTHEADER firstHeader) {
        this.firstHeader = firstHeader;
        notifyDataSetChanged();
    }

    public void setHeader(List<THEADER> header) {
        this.header = header;
        notifyDataSetChanged();
    }

    public void setFirstBody(List<TBODY> firstBody) {
        this.firstBody = firstBody;
        notifyDataSetChanged();
    }

    public void setBody(List<TBODY> body) {
        this.body = body;
        notifyDataSetChanged();
    }

    public void setSection(List<TBODY> section) {
        this.section = section;
        notifyDataSetChanged();
    }

    public List<THEADER> getHeader() {
        return header;
    }

    public List<TBODY> getBody() {
        return body;
    }

    @Override
    public int getRowCount() {
        return body.size();
    }

    @Override
    public int getColumnCount() {
        return header.size();
    }

    @Override
    public int getWidth(int column) {
        return getHeaderWidths().get(column + 1);
    }

    @Override public int getHeight(int row) {
        if (row == -1) return getHeaderHeight();
        else if (isSection(body, row)) return getSectionHeight();
        else return getBodyHeight();
    }

    /**
     * <ul>
     * <li>0 = First header (cell 0,0)</li>
     * <li>1 = Header (row 0)</li>
     * <li>2 = First body (column 0)</li>
     * <li>3 = Body (cell from 1,1 to N,N</li>
     * <li>4 = Section row (separator between rows)</li>
     * </ul>
     */
    @Override
    public int getItemViewType(int row, int column) {
        if (row == -1 && column == -1) return 0;
        else if (row == -1) return 1;
        else if (isSection(body, row)) return 4;
        else if (column == -1) return 2;
        else return 3;
    }

    /**
     * <ul>
     * <li>0 = First header (cell 0,0)</li>
     * <li>1 = Header (row 0)</li>
     * <li>2 = First body (column 0)</li>
     * <li>3 = Body (cell from 1,1 to N,N</li>
     * <li>4 = Section row (separator between rows)</li>
     * </ul>
     */
    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public View getView(int row, int column, View convertView, ViewGroup parent) {
        switch (getItemViewType(row, column)) {
            case 0: return getFirstHeader(row, column, convertView, parent);
            case 1: return getHeader(row, column, convertView, parent);
            case 2: return getFirstBody(row, column, convertView, parent);
            case 3: return getBody(row, column, convertView, parent);
            case 4: return getSection(row, column, convertView, parent);
            default: return null;
        }
    }
    private View getFirstHeader(final int row, final int column, View convertView, ViewGroup parent) {
        final VFIRSTHEADER vfirstheader = (convertView == null) ? inflateFirstHeader() : (VFIRSTHEADER) convertView;
        vfirstheader.bindFirstHeader(firstHeader);
        convertView = vfirstheader;
        if (clickListenerFirstHeader != null) convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListenerFirstHeader.onClickItem(firstHeader, vfirstheader, row, column);
            }
        });
        if (longClickListenerFirstHeader != null) convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListenerFirstHeader.onLongClickItem(firstHeader, vfirstheader, row, column);
                return true;
            }
        });
        return convertView;
    }

    private View getHeader(final int row, final int column, View convertView, ViewGroup parent) {
        final VHEADER vheader = (convertView == null) ? inflateHeader() : (VHEADER) convertView;
        vheader.bindHeader(header.get(column), column);
        convertView = vheader;
        if (clickListenerHeader != null) convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListenerHeader.onClickItem(header.get(column), vheader, row, column);
            }
        });
        if (longClickListenerHeader != null) convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListenerHeader.onLongClickItem(header.get(column), vheader, row, column);
                return true;
            }
        });
        return convertView;
    }

    private View getFirstBody(final int row, final int column, View convertView, ViewGroup parent) {
        final VFIRSTBODY vfirstbody = (convertView == null) ? inflateFirstBody() : (VFIRSTBODY) convertView;
        vfirstbody.bindFirstBody(firstBody.get(row), row);
        convertView = vfirstbody;
        if (clickListenerFirstBody != null) convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListenerFirstBody.onClickItem(firstBody.get(row), vfirstbody, row, column);
            }
        });
        if (longClickListenerFirstBody != null) convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListenerFirstBody.onLongClickItem(firstBody.get(row), vfirstbody, row, column);
                return true;
            }
        });
        return convertView;
    }

    private View getBody(final int row, final int column, View convertView, ViewGroup parent) {
        final VBODY vbody = (convertView == null) ? inflateBody() : (VBODY) convertView;
        vbody.bindBody(body.get(row), row, column);
        convertView = vbody;
        if (clickListenerBody != null) convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListenerBody.onClickItem(body.get(row), vbody, row, column);
            }
        });
        if (longClickListenerBody != null) convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListenerBody.onLongClickItem(body.get(row), vbody, row, column);
                return true;
            }
        });
        return convertView;
    }

    private View getSection(final int row, final int column, View convertView, ViewGroup parent) {
        final VSECTION vsection = (convertView == null) ? inflateSection() : (VSECTION) convertView;
        vsection.bindSection(section.get(row), row, column + 1);
        convertView = vsection;
        if (clickListenerSection != null) convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListenerSection.onClickItem(section.get(row), vsection, row, column);
            }
        });
        if (longClickListenerSection != null) convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListenerSection.onLongClickItem(section.get(row), vsection, row, column);
                return true;
            }
        });
        return convertView;
    }
}
