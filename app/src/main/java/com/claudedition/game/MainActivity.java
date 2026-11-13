package com.claudedition.game;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){super.onCreate(b);setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);setContentView(new Game());}
  class Game extends View {
    Paint p=new Paint(3); SharedPreferences prefs; Random rng=new Random(7);
    int page=0,level=0,mode=0,skin=0,selected=0,score=0,best=0,wins=0,attempts=0,editorTool=1;
    String[] modes={"CUBE","SHIP","BALL","UFO","WAVE"};
    String[] skins={"CYAN","PINK","GOLD","LIME","PURPLE","ICE","FIRE","MINT"};
    int[] colors={0xff00eaff,0xffff5b9d,0xffffd447,0xff9cff45,0xffbd83ff,0xfff4f7ff,0xffff713e,0xff55ffc4};
    int[][] patterns={{1,0,0,1,0,2,0,1,0,0,2,0,1,0,1,0,2,0,0,1,0,2,0,1,0,0,1,0,2,0,1,0,0,2},{1,2,0,1,0,2,1,0,2,0,1,2,0,1,0,2,1,0,1,2,0,1,2,0,1,0,2,1,0,2,1,0,2,1}};
    ArrayList<Integer> custom=new ArrayList<>();
    float ground,px=145,py,vy,scroll=0,last,holdX; boolean running=false,dead=false,won=false,holding=false,customLevel=false;
    Game(){super(MainActivity.this);prefs=getSharedPreferences("claude-edition",0);best=prefs.getInt("best",0);wins=prefs.getInt("wins",0);skin=prefs.getInt("skin",0);String s=prefs.getString("map","");for(String v:s.split(","))try{custom.add(Integer.parseInt(v));}catch(Exception e){}}
    void txt(Canvas c,String s,float x,float y,int size,int col){p.setColor(col);p.setTypeface(Typeface.create("sans-serif",1));p.setTextSize(size);c.drawText(s,x,y,p);}
    void center(Canvas c,String s,float x,float y,int size,int col){p.setTextSize(size);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(col);c.drawText(s,x-p.measureText(s)/2,y,p);}
    void rect(Canvas c,float x,float y,float w,float h,int col,float r){p.setColor(col);c.drawRoundRect(x,y,x+w,y+h,r,r,p);}
    void btn(Canvas c,float x,float y,float w,float h,String s,int id,int col){rect(c,x,y,w,h,col,13);center(c,s,x+w/2,y+h/2+7,Math.min(22,(int)(h*.4)),Color.WHITE);areas.add(new Area(x,y,x+w,y+h,id));}
    ArrayList<Area> areas=new ArrayList<>();
    void bg(Canvas c,int w,int h,int base){c.drawColor(base);p.setColor(0x22ffffff);for(int x=0;x<w;x+=48)for(int y=0;y<h;y+=48)c.drawCircle(x,y,1.5f,p);p.setColor(0xff25334f);for(int x=0;x<w;x+=80)c.drawLine(x,0,x,h,p);}
    @Override protected void onDraw(Canvas c){int w=getWidth(),h=getHeight();ground=h*.78f;areas.clear();
      if(page==0)home(c,w,h);else if(page==1)levels(c,w,h);else if(page==2)skins(c,w,h);else if(page==3)editor(c,w,h);else play(c,w,h);
      postInvalidateDelayed(page==4?16:200);
    }
    void home(Canvas c,int w,int h){bg(c,w,h,0xff10172e);center(c,"GEOMETRY DASH",w/2f,h*.25f,Math.min(56,w/12),Color.WHITE);center(c,"CLAUDE EDITION",w/2f,h*.34f,24,0xff00eaff);rect(c,w/2f-25,h*.41f,50,50,colors[skin],8);btn(c,w*.36f,h*.55f,w*.28f,48,"PLAY",1,0xff087da6);btn(c,w*.12f,h*.70f,w*.22f,44,"SKINS",2,0xff7542a8);btn(c,w*.39f,h*.70f,w*.22f,44,"LEVEL EDITOR",3,0xff9a4c37);txt(c,"WINS "+wins+"     BEST "+best,w*.39f,h*.91f,18,Color.LTGRAY);}
    void header(Canvas c,String s,int w){txt(c,s,28,40,28,Color.WHITE);btn(c,w-105,14,78,36,"BACK",0,0xff414b63);}
    void levels(Canvas c,int w,int h){bg(c,w,h,0xff111b32);header(c,"SELECT LEVEL",w);for(int i=0;i<2;i++){float x=w*(.22f+i*.32f);rect(c,x,h*.28f,w*.22f,h*.40f,i==0?0xff178e9d:0xff9b3b54,18);center(c,""+(i+1),x+w*.11f,h*.43f,54,Color.WHITE);center(c,i==0?"GRASSLAND":"THE NETHER",x+w*.11f,h*.53f,19,Color.WHITE);center(c,i==0?"NORMAL":"HARD",x+w*.11f,h*.60f,15,Color.WHITE);areas.add(new Area(x,h*.28f,x+w*.22f,h*.68f,10+i));}for(int m=0;m<5;m++){float mx=w*.10f+m*w*.16f;btn(c,mx,h*.73f,w*.14f,32,modes[m],50+m,mode==m?0xff168c9b:0xff46516e);}btn(c,w*.40f,h*.84f,w*.20f,36,"PLAY TEST",13,0xff5c458e);}
    void skins(Canvas c,int w,int h){bg(c,w,h,0xff14172d);header(c,"CUBE SKINS",w);for(int i=0;i<8;i++){int col=i%4,row=i/4;float x=w*.17f+col*w*.18f,y=h*.25f+row*h*.34f;rect(c,x,y,48,48,colors[i],8);center(c,skins[i],x+24,y+72,14,Color.WHITE);btn(c,x+60,y+5,100,38,i==skin?"EQUIPPED":"SELECT",20+i,i==skin?0xff287e60:0xff46516e);}}
    void editor(Canvas c,int w,int h){bg(c,w,h,0xff10172e);header(c,"LEVEL EDITOR",w);txt(c,"Tap cells to place blocks / spikes. Choose tool below.",24,76,16,Color.LTGRAY);float left=w*.08f,top=h*.18f,cw=w*.84f/16,ch=h*.43f/6;for(int y=0;y<6;y++)for(int x=0;x<16;x++){int i=y*16+x;p.setColor(0xff39445e);p.setStyle(Paint.Style.STROKE);c.drawRect(left+x*cw,top+y*ch,left+(x+1)*cw,top+(y+1)*ch,p);p.setStyle(Paint.Style.FILL);int v=i<custom.size()?custom.get(i):0;if(v>0)rect(c,left+x*cw+2,top+y*ch+2,cw-4,ch-4,v==1?colors[skin]:0xffff4f72,3);}btn(c,w*.12f,h*.70f,w*.17f,38,"BLOCK",30,0xff177e9a);btn(c,w*.32f,h*.70f,w*.17f,38,"SPIKE",31,0xff9c3657);btn(c,w*.52f,h*.70f,w*.17f,38,"ERASE",32,0xff505c75);btn(c,w*.72f,h*.70f,w*.17f,38,"SAVE",33,0xff287e60);btn(c,w*.39f,h*.82f,w*.22f,40,"TEST LEVEL",34,0xff7542a8);}
    void start(int l,boolean customMap){level=l;customLevel=customMap;page=4;running=false;dead=false;won=false;score=0;scroll=0;py=ground-42;vy=0;last=0;attempts++;invalidate();}
    void play(Canvas c,int w,int h){int sky=level==0?0xff183d49:0xff381b35;bg(c,w,h,sky);float dt=0;if(running&&!dead&&!won){long now=System.nanoTime();dt=last==0?0:Math.min(.035f,(now-last)/1e9f);last=now;physics(dt,w);scroll+=dt*310;score=(int)(scroll/35);if(score>best){best=score;prefs.edit().putInt("best",best).apply();}}
      rect(c,0,ground,w,h-ground,level==0?0xff245d40:0xff742b28,0);
      for(int i=0;i<w/50+3;i++){float x=(i*50-(scroll%50));rect(c,x,ground+10,48,5,0x55ffffff,1);}
      if(customLevel)drawCustom(c,w);else drawObstacles(c,w);
      drawPlayer(c);txt(c,"LEVEL "+(level+1)+"   "+modes[mode],22,32,17,Color.WHITE);txt(c,""+score,w-90,32,19,Color.WHITE);
      if(!running&&!dead&&!won)center(c,"TAP TO START",w/2f,h*.35f,28,Color.WHITE);
      if(dead){center(c,"TRY AGAIN!",w/2f,h*.40f,34,Color.WHITE);btn(c,w*.38f,h*.53f,w*.24f,44,"RETRY",40,0xffb23e60);btn(c,w*.38f,h*.66f,w*.24f,40,"MENU",0,0xff414b63);}
      if(won){center(c,"LEVEL COMPLETE!",w/2f,h*.40f,32,Color.WHITE);btn(c,w*.38f,h*.55f,w*.24f,44,"NEXT",41,0xff287e60);btn(c,w*.38f,h*.68f,w*.24f,40,"MENU",0,0xff414b63);}
    }
    void physics(float dt,int w){float floor=ground-42;float speed=310+level*38;scroll+=0; if(mode==0||mode==2){vy+=1850*dt;py+=vy*dt;if(py>floor){py=floor;vy=0;}}else if(mode==1){vy+=(holding?-1150:1250)*dt;py+=vy*dt;py=Math.max(35,Math.min(floor,py));}else if(mode==3){vy+=1250*dt;py+=vy*dt;if(py>floor){py=floor;vy=0;}}else{vy=(holding?-520:520);py+=vy*dt;py=Math.max(35,Math.min(floor,py));}
      float ox=w-((scroll+speed*dt*60)% (w+220));if(customLevel){for(int i=0;i<custom.size();i++){int v=custom.get(i);if(v!=0){float x=w*.08f+(i%16)*(w*.84f/16)-scroll%w;float top=ground-(i/16+1)*34f;if(x<px+42&&x+38>px&&py+42>top&&py<top+38)dead=true;}}}
      else{int[] pat=patterns[level];for(int i=0;i<pat.length;i++){float x=w+180+i*145-scroll%(w+pat.length*145);if(pat[i]==1&&x<px+42&&x+36>px&&py+42>ground-42)dead=true;if(pat[i]==2&&x<px+42&&x+40>px&&py<ground-100)dead=true;}}
      if(scroll> (customLevel?2300:patLength()*145+1800)){won=true;running=false;wins++;prefs.edit().putInt("wins",wins).apply();}
      if(py<22||py>ground+80)dead=true;
    }
    int patLength(){return patterns[level].length;}
    void drawObstacles(Canvas c,int w){int[] pat=patterns[level];for(int i=0;i<pat.length;i++){float x=w+180+i*145-scroll%(w+pat.length*145);if(pat[i]==1){p.setColor(0xffff5274);Path t=new Path();t.moveTo(x,ground);t.lineTo(x+19,ground-42);t.lineTo(x+38,ground);t.close();c.drawPath(t,p);}else if(pat[i]==2){rect(c,x,ground-118,36,14,0xffe9b84a,3);}}}
    void drawCustom(Canvas c,int w){float cw=w*.84f/16;for(int i=0;i<custom.size();i++){int v=custom.get(i);if(v==0)continue;float x=w*.08f+(i%16)*cw-scroll%w;float top=ground-(i/16+1)*34f;if(v==1)rect(c,x,top,cw-2,30,colors[skin],2);else{p.setColor(0xffff5274);Path t=new Path();t.moveTo(x,top+34);t.lineTo(x+cw/2,top);t.lineTo(x+cw,top+34);t.close();c.drawPath(t,p);}}}
    void drawPlayer(Canvas c){p.setColor(colors[skin]);c.save();float cx=px+21,cy=py+21;if(mode==0){c.rotate((float)(scroll*.18%360),cx,cy);c.drawRoundRect(px,py,px+42,py+42,6,6,p);}else if(mode==1){Path ship=new Path();ship.moveTo(px,py+7);ship.lineTo(px+42,cy);ship.lineTo(px,py+35);ship.lineTo(px+9,cy);ship.close();c.drawPath(ship,p);p.setColor(0xffffd447);Path flame=new Path();flame.moveTo(px+5,cy-7);flame.lineTo(px-10,cy);flame.lineTo(px+5,cy+7);flame.close();c.drawPath(flame,p);}else if(mode==2){c.rotate((float)(scroll*.25%360),cx,cy);c.drawCircle(cx,cy,21,p);p.setColor(0xff26334c);c.drawCircle(cx,cy,8,p);}else if(mode==3){c.drawOval(px,py+7,px+42,py+38,p);p.setColor(0xffeafaff);c.drawArc(px+5,py-2,px+37,py+25,180,180,false,p);c.drawRect(px+11,py+21,px+16,py+26,p);c.drawRect(px+26,py+21,px+31,py+26,p);}else{Path wave=new Path();wave.moveTo(cx,py);wave.lineTo(px+42,cy);wave.lineTo(cx,py+42);wave.lineTo(px,cy);wave.close();c.drawPath(wave,p);}if(mode!=1){p.setColor(Color.WHITE);c.drawCircle(px+14,py+17,3,p);c.drawCircle(px+29,py+17,3,p);}c.restore();}
    void action(int id){if(id==0){page=0;}else if(id==1)page=1;else if(id==2)page=2;else if(id==3)page=3;else if(id==10||id==11)start(id-10,false);else if(id==13)start(0,false);else if(id>=50&&id<55)mode=id-50;else if(id>=20&&id<28){skin=id-20;prefs.edit().putInt("skin",skin).apply();}else if(id==30)editorTool=1;else if(id==31)editorTool=2;else if(id==32)editorTool=0;else if(id==33){StringBuilder b=new StringBuilder();for(int v:custom){if(b.length()>0)b.append(',');b.append(v);}prefs.edit().putString("map",b.toString()).apply();}else if(id==34)start(0,true);else if(id==40)start(level,customLevel);else if(id==41)start((level+1)%2,false);invalidate();}
    @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();if(page==4){if(e.getAction()==MotionEvent.ACTION_DOWN){if(dead||won){for(Area a:areas)if(a.hit(x,y)){action(a.id);return true;}}else{holding=true;if(!running){running=true;last=0;}if(mode==3)vy=-520;else if(mode==0||mode==2){if(py>=ground-43)vy=mode==2?-730:-690;} }return true;}if(e.getAction()==MotionEvent.ACTION_UP){holding=false;return true;}return true;}
      if(e.getAction()==MotionEvent.ACTION_DOWN){if(page==3){float left=getWidth()*.08f,top=getHeight()*.18f,cw=getWidth()*.84f/16,ch=getHeight()*.43f/6;if(y>=top&&y<top+ch*6&&x>=left&&x<left+cw*16){int idx=(int)((y-top)/ch)*16+(int)((x-left)/cw);while(custom.size()<96)custom.add(0);custom.set(idx,editorTool);invalidate();return true;}}
      for(int i=areas.size()-1;i>=0;i--)if(areas.get(i).hit(x,y)){action(areas.get(i).id);return true;}}return true;}
    class Area{float l,t,r,b;int id;Area(float a,float z,float q,float d,int n){l=a;t=z;r=q;b=d;id=n;}boolean hit(float x,float y){return x>=l&&x<=r&&y>=t&&y<=b;}}
  }
}