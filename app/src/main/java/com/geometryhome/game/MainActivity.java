package com.geometryhome.game;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){super.onCreate(b);setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);getWindow().setFlags(1024,1024);setContentView(new HomeGame());}

  class Hazard { float x, y, w, h, phase; int type; Hazard(float a,int t,float floor){x=a;type=t;w=t==3||t==4?48:(t==1?76:44);h=t==2?48:(t==3||t==4?48:42);y=floor-h;phase=(float)Math.random()*6.28f;} }
  class HomeGame extends View {
    Paint p=new Paint(3); Random rng=new Random(); SharedPreferences prefs;
    int page=0, level=0, skin=0, score=0, best=0, coins=0, deaths=0, completed=0;
    int[] colors={Color.rgb(0,235,255),Color.rgb(255,75,160),Color.rgb(255,205,55),Color.rgb(110,255,115),Color.rgb(180,110,255),Color.WHITE};
    String[] levelNames={"НЕОНОВЫЙ РАЗГОН","ЛАВОВЫЙ ПОТОК","КОСМИЧЕСКИЙ ХАОС","ФИНАЛЬНЫЙ РЫВОК"};
    int[] levelColors={0xff10254c,0xff431b35,0xff20164c,0xff102e32};
    ArrayList<Hazard> hazards=new ArrayList<>(); ArrayList<RectF> buttons=new ArrayList<>(); ArrayList<String> actions=new ArrayList<>();
    float floor, px=135, py, vy, speed, spawnX, distance, time, last, shake;
    boolean running=false, dead=false, won=false, custom=false;
    HomeGame(){super(MainActivity.this);prefs=getSharedPreferences("geometryhome_save",0);best=prefs.getInt("best",0);coins=prefs.getInt("coins",0);completed=prefs.getInt("completed",0);skin=prefs.getInt("skin",0);}
    void save(){prefs.edit().putInt("best",best).putInt("coins",coins).putInt("completed",completed).putInt("skin",skin).apply();}
    void txt(Canvas c,String s,float x,float y,int size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));c.drawText(s,x,y,p);}
    void mid(Canvas c,String s,float x,float y,int size,int color){p.setTextSize(size);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(color);c.drawText(s,x-p.measureText(s)/2,y,p);}
    void bg(Canvas c,int w,int h,int col){c.drawColor(col);p.setColor(0x22ffffff);for(int x=15;x<w;x+=48)for(int y=15;y<h;y+=48)c.drawCircle(x,y,1.7f,p);p.setColor(0x1820dfff);for(int i=0;i<5;i++)c.drawCircle((i*193+(int)(distance*.12f))%(w+100),h*.22f+i*31,18+i*8,p);}
    void btn(Canvas c,float x,float y,float w,float h,String label,String act,int col){RectF r=new RectF(x,y,x+w,y+h);p.setColor(col);c.drawRoundRect(r,15,15,p);p.setColor(0x66ffffff);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawRoundRect(r,15,15,p);p.setStyle(Paint.Style.FILL);mid(c,label,x+w/2,y+h/2+7,Math.min(21,(int)(h*.37f)),Color.WHITE);buttons.add(r);actions.add(act);}
    @Override protected void onDraw(Canvas c){int w=getWidth(),h=getHeight();floor=h*.79f;buttons.clear();actions.clear();if(page==0)home(c,w,h);else if(page==1)levels(c,w,h);else if(page==2)skins(c,w,h);else if(page==3)stats(c,w,h);else game(c,w,h);postInvalidateDelayed(page==4?16:180);}
    void home(Canvas c,int w,int h){bg(c,w,h,0xff07152d);mid(c,"ГЕОМЕТРИ ДОМ",w/2f,h*.26f,Math.min(58,w/12),Color.WHITE);mid(c,"УЛЬТРА-ПОЛОСА ПРЕПЯТСТВИЙ",w/2f,h*.35f,18,0xff00eaff);p.setColor(colors[skin]);c.save();c.rotate(16,w/2f,h*.49f);c.drawRoundRect(w/2f-26,h*.49f-26,w/2f+26,h*.49f+26,8,8,p);c.restore();btn(c,w*.35f,h*.57f,w*.30f,48,"ИГРАТЬ","levels",0xff087eae);btn(c,w*.16f,h*.72f,w*.20f,43,"СКИНЫ","skins",0xff9b4a9c);btn(c,w*.42f,h*.72f,w*.20f,43,"РЕКОРДЫ","stats",0xff277b58);txt(c,"РЕКОРД "+best+"     МОНЕТЫ "+coins,w*.39f,h*.93f,17,Color.LTGRAY);}
    void header(Canvas c,String s,int w){txt(c,s,26,40,27,Color.WHITE);btn(c,w-105,14,80,36,"НАЗАД","home",0xff3a4964);}
    void levels(Canvas c,int w,int h){bg(c,w,h,0xff09172e);header(c,"ВЫБЕРИ ТРАССУ",w);for(int i=0;i<4;i++){float x=w*.075f+i*w*.225f;p.setColor(levelColors[i]);c.drawRoundRect(x,h*.28f,x+w*.19f,h*.68f,18,18,p);mid(c,"0"+(i+1),x+w*.095f,h*.43f,40,colors[i]);mid(c,levelNames[i],x+w*.095f,h*.53f,13,Color.WHITE);mid(c,new String[]{"ЛЁГКАЯ","СРЕДНЯЯ","СЛОЖНАЯ","ЭКСТРИМ"}[i],x+w*.095f,h*.61f,14,0xffb9eaff);buttons.add(new RectF(x,h*.28f,x+w*.19f,h*.68f));actions.add("level"+i);}}
    void skins(Canvas c,int w,int h){bg(c,w,h,0xff10152e);header(c,"ВЫБЕРИ КУБ",w);for(int i=0;i<6;i++){int col=i%3,row=i/3;float x=w*.23f+col*w*.19f,y=h*.24f+row*h*.31f;p.setColor(colors[i]);c.drawRoundRect(x,y,x+48,y+48,9,9,p);mid(c,new String[]{"НЕОН","РОЗОВЫЙ","ЗОЛОТО","ЛАЙМ","АМЕТИСТ","ЛЁД"}[i],x+24,y+73,15,Color.WHITE);btn(c,x+60,y+4,105,36,i==skin?"ВЫБРАН":"ВЫБРАТЬ","skin"+i,skin==i?0xff25805a:0xff485b83);}}
    void stats(Canvas c,int w,int h){bg(c,w,h,0xff10152e);header(c,"ТВОИ РЕЗУЛЬТАТЫ",w);mid(c,"ЛУЧШИЙ СЧЁТ: "+best,w/2f,h*.34f,30,Color.WHITE);mid(c,"СОБРАНО МОНЕТ: "+coins,w/2f,h*.48f,25,0xffffdc55);mid(c,"ПРОЙДЕНО ТРАСС: "+completed,w/2f,h*.62f,25,0xff64f5d2);mid(c,"ПОПЫТОК: "+deaths,w/2f,h*.76f,20,Color.LTGRAY);}
    void start(int n){level=n;page=4;running=false;dead=false;won=false;score=0;distance=0;time=0;speed=420+n*75;py=0;vy=0;last=0;spawnX=getWidth()+200;hazards.clear();for(int i=0;i<5;i++)spawn(i);invalidate();}
    void spawn(int index){int t;int roll=rng.nextInt(100);if(roll<28)t=0;else if(roll<43)t=1;else if(roll<60)t=2;else if(roll<78)t=3;else if(roll<90)t=4;else if(roll<96)t=5;else t=6;float x=spawnX+index*(230+rng.nextInt(110));hazards.add(new Hazard(x,t,floor));}
    void jump(){if(dead||won)return;if(!running){running=true;last=0;}if(py>=-2){vy=-760;}}
    void update(float dt,int w){if(!running||dead||won)return;time+=dt;distance+=speed*dt;score=(int)(distance/80);speed+=dt*(3+level);py+=vy*dt;vy+=1950*dt;if(py>0){py=0;vy=0;}for(Hazard z:hazards){z.x-=speed*dt;if(z.type==4)z.y=floor-z.h-55+(float)Math.sin(time*4+z.phase)*27;if(z.type==6)z.y=floor-z.h-8;}
      if(hazards.size()>0&&hazards.get(0).x<-100){hazards.remove(0);spawn(hazards.size());}
      float top=floor-44+py;RectF player=new RectF(px,top,px+42,top+42);
      for(Hazard z:hazards){RectF hit=new RectF(z.x+7,z.y+7,z.x+z.w-7,z.y+z.h-3);if(z.type==5){if(player.right>z.x&&player.left<z.x+z.w&&player.bottom>floor-10){die();break;}}else if(RectF.intersects(player,hit)){if(z.type==6){vy=-1050;z.x=-300;}else{die();break;}}}
      if(distance>5200+level*500){won=true;running=false;completed++;coins+=10+level*5;save();}
      if(score>best){best=score;save();}
    }
    void die(){dead=true;running=false;deaths++;shake=10;}
    void drawHazard(Canvas c,Hazard z){p.setColor(z.type==3||z.type==4?0xffff4d86:(z.type==6?0xffffd34d:0xffff456d));if(z.type==0||z.type==1){Path t=new Path();t.moveTo(z.x,z.y+z.h);t.lineTo(z.x+z.w/2,z.y);t.lineTo(z.x+z.w,z.y+z.h);t.close();c.drawPath(t,p);if(z.type==1){Path t2=new Path();t2.moveTo(z.x+34,z.y+z.h);t2.lineTo(z.x+57,z.y+7);t2.lineTo(z.x+z.w,z.y+z.h);t2.close();c.drawPath(t2,p);}}else if(z.type==2){c.drawRoundRect(z.x,z.y,z.x+z.w,z.y+z.h,5,5,p);}else if(z.type==3||z.type==4){c.drawCircle(z.x+z.w/2,z.y+z.h/2,z.w/2,p);p.setColor(0xff25263d);c.drawCircle(z.x+z.w/2,z.y+z.h/2,13,p);p.setColor(Color.WHITE);c.drawCircle(z.x+z.w/2,z.y+z.h/2,4,p);}else if(z.type==5){p.setColor(0xff070d1c);c.drawRect(z.x,floor,z.x+z.w,floor+35,p);}else{p.setColor(0xffffdf54);c.drawRoundRect(z.x,z.y,z.x+z.w,z.y+z.h,7,7,p);}}
    void game(Canvas c,int w,int h){int color=levelColors[level];bg(c,w,h,color);if(running){long now=System.nanoTime();float dt=last==0?0:Math.min(.035f,(now-last)/1e9f);last=now;update(dt,w);}else last=0;
      p.setColor(0xff00eaff);p.setStrokeWidth(4);c.drawLine(0,floor,w,floor,p);for(Hazard z:hazards)drawHazard(c,z);
      float top=floor-44+py;p.setColor(colors[skin]);c.save();c.rotate((float)(distance*.13)%360,px+21,top+21);c.drawRoundRect(px,top,px+42,top+42,7,7,p);c.restore();p.setColor(Color.WHITE);c.drawCircle(px+28,top+13,3,p);
      txt(c,levelNames[level],22,30,17,Color.WHITE);txt(c,"СЧЁТ "+score,w-145,30,18,Color.WHITE);p.setColor(0xff00eaff);c.drawRoundRect(w*.32f,13,w*.68f,20,4,4,p);p.setColor(0x55ffffff);c.drawRoundRect(w*.32f,13,w*.68f,20,4,4,p);p.setColor(0xff00eaff);c.drawRoundRect(w*.32f,13,w*.32f+(w*.36f)*Math.min(1,distance/(5200+level*500)),20,4,4,p);
      if(!running&&!dead&&!won){mid(c,"КОСНИСЬ ЭКРАНА ДЛЯ СТАРТА",w/2f,h*.48f,24,Color.WHITE);mid(c,"ПРЫЖОК — КАСАНИЕ ЭКРАНА",w/2f,h*.57f,15,0xffbceeff);}
      if(dead){mid(c,"СТОЛКНОВЕНИЕ!",w/2f,h*.42f,34,Color.WHITE);btn(c,w*.37f,h*.55f,w*.26f,44,"ЕЩЁ РАЗ","retry",0xffb8325b);btn(c,w*.37f,h*.67f,w*.26f,40,"В МЕНЮ","home",0xff394967);}
      if(won){mid(c,"ТРАССА ПРОЙДЕНА!",w/2f,h*.42f,34,Color.WHITE);mid(c,"+ "+(10+level*5)+" МОНЕТ",w/2f,h*.50f,20,0xffffdf54);btn(c,w*.37f,h*.59f,w*.26f,44,"СЛЕДУЮЩАЯ","next",0xff16885e);btn(c,w*.37f,h*.71f,w*.26f,40,"В МЕНЮ","home",0xff394967);}
    }
    void act(String a){if(a.equals("home")){page=0;running=false;}else if(a.equals("levels"))page=1;else if(a.equals("skins"))page=2;else if(a.equals("stats"))page=3;else if(a.startsWith("level"))start(Integer.parseInt(a.substring(5)));else if(a.startsWith("skin")){int s=Integer.parseInt(a.substring(4));if(s==0||coins>=s*8){skin=s;if(s>0)coins-=s*8;save();}}else if(a.equals("retry"))start(level);else if(a.equals("next"))start((level+1)%4);invalidate();}
    @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float x=e.getX(),y=e.getY();if(page==4){if(dead||won){for(int i=buttons.size()-1;i>=0;i--)if(buttons.get(i).contains(x,y)){act(actions.get(i));return true;}}else jump();}else{for(int i=buttons.size()-1;i>=0;i--)if(buttons.get(i).contains(x,y)){act(actions.get(i));return true;}}invalidate();return true;}
  }
}