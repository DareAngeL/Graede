package com.tajos.studio.graphics;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.awt.geom.PathIterator.WIND_NON_ZERO;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author Rene Tajos Jr.
 */
public abstract class RoundedShapePath extends RectangularShape {
        public abstract double getArcHeight();
        public abstract double getArcWidth();
        public abstract double getTopLeftArc();
        public abstract double getTopRightArc();
        public abstract double getBtmLeftArc();
        public abstract double getBtmRightArc();

        public abstract void setRoundRect(double x, double y, double w, double h,double arcWidth, double arcHeight);

        protected RoundedShapePath() {}

       /**
        * Return a new path iterator which iterates over this rectangle.
        * 
        * @param at An affine transform to apply to the object
        */
        @Override
       public PathIterator getPathIterator(final AffineTransform at)
       {
         double arcW = Math.min(getArcWidth(), getWidth());
         double arcH = Math.min(getArcHeight(), getHeight());
         
         // look for a possible path
         if (arcW <= 0 || arcH <= 0)
           {
             Rectangle2D r = new Rectangle2D.Double(getX(), getY(), getWidth(), 
                     getHeight());
             return r.getPathIterator(at);
           }
         else if (arcW >= getWidth() && arcH >= getHeight()) 
           {
             Ellipse2D e = new Ellipse2D.Double(getX(), getY(), getWidth(), 
                     getHeight());
             return e.getPathIterator(at);
           }

         // otherwise return the standard path...
         return new PathIterator() 
           {
             double x = getX();
             double y = getY();
             double w = getWidth();
             double h = getHeight();
             double arcW = Math.min(getArcWidth(), w);
             double arcH = Math.min(getArcHeight(), h);
             double topLeftArc = getTopLeftArc();
             double topRightArc = getTopRightArc();
             double btmLeftArc = getBtmLeftArc();
             double btmRightArc = getBtmRightArc();
             PathIterator corner;
             Arc2D.Double arc = new Arc2D.Double();
             int step = -1;

             @Override
             public int currentSegment(double[] coords) 
             {
               if (corner != null) // steps 1, 3, 5 and 7
               {
                 int r = corner.currentSegment(coords);
                 if (r == SEG_MOVETO)
                    r = SEG_LINETO;
                 return r;
               }
                 switch (step) {
                     case -1 -> {
                         // move to the start position
                         coords[0] = x + w - arcW / 2;
                         coords[1] = y;
                     }
                     case 0 -> {
                         // top line
                         coords[0] = x + arcW / 2;
                         coords[1] = y;
                     }
                     case 2 -> {
                         // left line
                         coords[0] = x;
                         coords[1] = y + h - arcH / 2;
                     }
                     case 4 -> {
                         // bottom line
                         coords[0] = x + w - arcW / 2;
                         coords[1] = y + h;
                     }
                     case 6 -> {
                         // right line
                         coords[0] = x + w;
                         coords[1] = y + arcH / 2;
                     }
                     default -> {
                     }
                 }
               if (at != null)
                 at.transform(coords, 0, coords, 0, 1);

               return step == -1 ? SEG_MOVETO : SEG_LINETO;
             }

           @Override
           public int getWindingRule() {
             return WIND_NON_ZERO;
           }

             @Override
           public boolean isDone() {
             return step >= 8;
           }

           @Override
           public void next() 
           {
              if (corner != null)
               {
                 corner.next();
                 if (corner.isDone())
                   {
                     corner = null;
                     step++;
                   }
               }
             else
               {
                 step++;
                  switch (step) {
                      case 1 -> {
                          // create top left corner
                          double topLeftArcW = Math.min(topLeftArc, w);
                          double topLeftArcH = Math.min(topLeftArc, h);

                          arc.setArc(x, y, topLeftArcW, topLeftArcH, 90, 90, Arc2D.OPEN);
                          corner = arc.getPathIterator(at);
                      }
                      case 3 -> {
                          // create bottom left corner
                          double btmLeftArcW = Math.min(btmLeftArc, w);
                          double btmLeftArcH = Math.min(btmLeftArc, h);

                          arc.setArc(x, y + h - btmLeftArcH, btmLeftArcW, btmLeftArcH, 180, 90,
                                  Arc2D.OPEN);
                          corner = arc.getPathIterator(at);
                      }
                      case 5 -> {
                          // create bottom right corner
                          double btmRightArcW = Math.min(btmRightArc, w);
                          double btmRightArcH = Math.min(btmRightArc, h);

                          arc.setArc(x + w - btmRightArcW, y + h - btmRightArcH, btmRightArcW, btmRightArcH, 270, 90,
                                  Arc2D.OPEN);
                          corner = arc.getPathIterator(at);
                      }
                      case 7 -> {
                          // create top right corner
                          double topRightArcW = Math.min(topRightArc, w);
                          double topRightArcH = Math.min(topRightArc, h);

                          arc.setArc(x + w - topRightArcW, y, topRightArcW, topRightArcH, 0, 90, Arc2D.OPEN);
                          corner = arc.getPathIterator(at);
                      }
                       default -> {
                      }
                  }
               }
           }

             @Override
             public int currentSegment(float[] coords) {
                 return -1;
             }
         };
       }

        @Override
        public boolean intersects(double x, double y, double w, double h)
       {
         // Check if any corner is within the rectangle
         return (contains(x, y) || contains(x, y + h) || contains(x + w, y + h)
                || contains(x + w, y));
       }

        @Override
        public void setFrame(double x, double y, double w, double h)
       {
         setRoundRect(x, y, w, h, getArcWidth(), getArcHeight());
       }

        public void setRoundRect(RoundRectangle2D rr)
       {
         setRoundRect(rr.getX(), rr.getY(), rr.getWidth(), rr.getHeight(),
                      rr.getArcWidth(), rr.getArcHeight());
       }

       public static class Create extends RoundedShapePath {

            public Corners corners;

            public double archHeight;
            public double archWidth;
            public double x;
            public double y;
            public double width;
            public double height;

            public Create() {}

            public Create(double x, double y, double w, double h, Corners corners) {
                this.x = x;
                this.y = y;
                this.width = w;
                this.height = h;
                this.archHeight = corners.Arch();
                this.archWidth = corners.Arch();
                this.corners = corners;
            }

            @Override
            public double getArcHeight() {
                return archHeight;
            }

            @Override
            public double getArcWidth() {
                return archWidth;
            }

            @Override
            public double getTopLeftArc() {
                return corners.TopLeft();
            }

            @Override
            public double getTopRightArc() {
                return corners.TopRight();
            }

            @Override
            public double getBtmLeftArc() {
                return corners.BtmLeft();
            }

            @Override
            public double getBtmRightArc() {
                return corners.BtmRight();
            }

            @Override
            public void setRoundRect(double x, double y, double w, double h, double arcWidth, double arcHeight) {}

            @Override
            public double getX() {
               return x;
            }

            @Override
            public double getY() {
               return y;
            }

            @Override
            public double getWidth() {
               return width;
            }

            @Override
            public double getHeight() {
                return height;
            }

            @Override
            public boolean isEmpty() {
                return width <= 0 || height <= 0;
            }

            @Override
            public Rectangle2D getBounds2D() {
                return new Rectangle2D.Double(x, y, width, height);
            }

            @Override
            public boolean contains(double x, double y) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean contains(double x, double y, double w, double h) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
       }

       public static class Corners {
           protected final int mTopLeft;
           protected final int mTopRight;
           protected final int mBtmLeft;
           protected final int mBtmRight;

           public Corners(int topLeft, int topRight, int btmLeft, int btmRight) {
               mTopLeft = topLeft;
               mTopRight = topRight;
               mBtmLeft = btmLeft;
               mBtmRight = btmRight;
           }
           /*
           * get the highest possible radius just to fill the arcW and arcH
           */
           public double Arch() {
               if (mTopLeft >= mTopRight &&
                   mTopLeft >= mBtmLeft &&
                   mTopLeft >= mBtmRight) {

                   if (isAllCornerZero())
                       return 0;

                   return (double) mTopLeft == 0? 5 : mTopLeft;
                }

               if (mTopRight >= mTopLeft &&
                   mTopRight >= mBtmLeft &&
                   mTopRight >= mBtmRight) {

                   if (isAllCornerZero())
                       return 0;

                   return (double) mTopRight == 0? 5 : mTopRight;
               }

               if (mBtmLeft >= mTopLeft &&
                   mBtmLeft >= mTopRight &&
                   mBtmLeft >= mBtmRight) {

                   if (isAllCornerZero())
                       return 0;

                   return (double) mBtmLeft == 0? 5 : mBtmLeft;
               }

               if (mBtmRight >= mTopLeft &&
                   mBtmRight >= mTopRight &&
                   mBtmRight >= mBtmLeft) {

                   if (isAllCornerZero())
                       return 0;

                   return (double) mBtmRight == 0? 5 : mBtmRight;
               }

               return -1;
           }

           private boolean isAllCornerZero() {
               return mTopRight == 0 &&
                       mTopLeft == 0 &&
                       mBtmLeft == 0 &&
                       mBtmRight == 0;
           }

           public int TopLeft() {
               return mTopLeft;
           }

           public int TopRight() {
               return mTopRight;
           }

           public int BtmLeft() {
               return mBtmLeft;
           }

           public int BtmRight() {
               return mBtmRight;
           }
       }
}
