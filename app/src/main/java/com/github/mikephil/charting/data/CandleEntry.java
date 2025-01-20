
package com.github.mikephil.charting.data;

/**
 * Subclass of Entry that holds all values for one entry in a CandleStickChart.
 * 
 * @author Philipp Jahoda
 */
public class CandleEntry extends Entry {

    /** shadow-high value */
    private float mShadowHigh = 0f;

    /** shadow-low value */
    private float mShadowLow = 0f;

    /** close value */
    private float mClose = 0f;

    /** open value */
    private float mOpen = 0f;
//Modify for stock    
    private String mTag = "";
//Modify for stock
    /**
     * Constructor.
     * 
     * @param xIndex The index on the x-axis.
     * @param shadowH The (shadow) high value.
     * @param shadowL The (shadow) low value.
     * @param open The open value.
     * @param close The close value.
     */
//Modify for stock     
    public CandleEntry(int xIndex, float shadowH, float shadowL, float open, float close, String tag) {
//Modify for stock     	
        super((shadowH + shadowL) / 2f, xIndex);

        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
      //Modify for stock        
        this.mTag = tag;
      //Modify for stock
    }

    /**
     * Constructor.
     * 
     * @param xIndex The index on the x-axis.
     * @param shadowH The (shadow) high value.
     * @param shadowL The (shadow) low value.
     * @param open
     * @param close
     * @param data Spot for additional data this Entry represents.
     */
//Modify for stock    
    public CandleEntry(int xIndex, float shadowH, float shadowL, float open, float close,
            Object data, String tag) {
//Modify for stock    	
        super((shadowH + shadowL) / 2f, xIndex, data);

        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
        this.mTag = tag;        
    }

    /**
     * Returns the overall range (difference) between shadow-high and
     * shadow-low.
     * 
     * @return
     */
    public float getShadowRange() {
        return Math.abs(mShadowHigh - mShadowLow);
    }

    /**
     * Returns the body size (difference between open and close).
     * 
     * @return
     */
    public float getBodyRange() {
        return Math.abs(mOpen - mClose);
    }

    /**
     * Returns the center value of the candle. (Middle value between high and
     * low)
     */
    @Override
    public float getVal() {
        return super.getVal();
    }

    public CandleEntry copy() {
//Modify for stock
        CandleEntry c = new CandleEntry(getXIndex(), mShadowHigh, mShadowLow, mOpen,
                mClose, getData(), mTag);
//Modify for stock
        return c;
    }

    /**
     * Returns the upper shadows highest value.
     * 
     * @return
     */
    public float getHigh() {
        return mShadowHigh;
    }

    public void setHigh(float mShadowHigh) {
        this.mShadowHigh = mShadowHigh;
    }

    /**
     * Returns the lower shadows lowest value.
     * 
     * @return
     */
    public float getLow() {
        return mShadowLow;
    }

    public void setLow(float mShadowLow) {
        this.mShadowLow = mShadowLow;
    }

    /**
     * Returns the bodys close value.
     * 
     * @return
     */
    public float getClose() {
        return mClose;
    }

    public void setClose(float mClose) {
        this.mClose = mClose;
    }

    /**
     * Returns the bodys open value.
     * 
     * @return
     */
    public float getOpen() {
        return mOpen;
    }

    public void setOpen(float mOpen) {
        this.mOpen = mOpen;
    }
    
//Modify for stock    
    public String getTag() {
        return mTag;
    }

    public void setTag(String mTag) {
        this.mTag = mTag;
    }
//Modify for stock
}
