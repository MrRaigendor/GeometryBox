package com.geometrybox.game;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
  Game game;
  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    getWindow().setFlags(1024,1024);
    game = new Game();
    setContentView(game);
  }

  class Game extends View {
    Paint p = new Paint(3);
    Random random = new Random();
    SharedPreferences prefs;
    String page="home";
    int selectedLevel=0, skin=0, palette=1, score=0, best=0, wins=0, attempts=0, jumps=0, saved=0;
    int bg=Color.rgb(10,13,38), accent=Color.rgb(0,225,255);
    float ground, playerY=0, velocity=0, obstacleX=0, speed=8, distance=0, lastTime=0;
    boolean playing=false, dead=false, won=false, custom=false;
    ArrayList<RectF> buttons=new ArrayList<>();
    ArrayList<String> actions=new ArrayList<>();
    ArrayList<Integer> creatorBlocks=new ArrayList<>();
    float[] obstaclePositions;
    int[] skinColors={Color.CYAN,Color.rgb(255,70,150),Color.YELLOW,Color.GREEN,Color.rgb(180,100,255),Color.WHITE};
    String[] skinNames={"Неон","Розовый","Золото","Лайм","Аметист","Лёд"};
    String[] levelNames={"Первый шаг","Неоновые улицы","Гравитация"};
    String[] levelDiff={"Лёгкий","Средний","Сложный"};
    int[] levelColors={Color.rgb(0,220,255),Color.rgb(255,65,145),Color.rgb(170,100,255)};
    Game(){super(MainActivity.this);prefs=getSharedPreferences("geometrybox",0);
      best=prefs.getInt("best",0); wins=prefs.getInt("wins",0);attempts=prefs.getInt("attempts",0);
      jumps=prefs.getInt("jumps",0);skin=prefs.getInt("skin",0);saved=prefs.getInt("saved",0);
      String data=prefs.getString("creator","");
      if(!data.isEmpty()){for(String s:data.split(",")){try{creatorBlocks.add(Integer.parseInt(s));}catch(Exception e){}}}
    }
    void save(){prefs.edit().putInt("best",best).putInt("wins",wins).putInt("attempts",attempts).putInt("jumps",jumps).putInt("skin",skin).putInt("saved",saved).apply();}
    void text(Canvas c,String s,float x,float y,int size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));c.drawText(s,x,y,p);}
    void center(Canvas c,String s,float x,float y,int size,int color){p.setTextSize(size);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(color);c.drawText(s,x-p.measureText(s)/2,y,p);}
    void button(Canvas c,float x,float y,float w,float h,String label,String action,int color){
      RectF r=new RectF(x,y,x+w,y+h);p.setColor(color);c.drawRoundRect(r,16,16,p);
      p.setColor(Color.argb(70,255,255,255));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawRoundRect(r,16,16,p);p.setStyle(Paint.Style.FILL);
      center(c,label,x+w/2,y+h/2+8,Math.min(23,(int)(h*.38f)),Color.WHITE);buttons.add(r);actions.add(action);
    }
    void background(Canvas c,int w,int h,int color){c.drawColor(color);p.setColor(Color.argb(35,255,255,255));for(int x=20;x<w;x+=55)for(int y=20;y<h;y+=55)c.drawCircle(x,y,2,p);}
    @Override protected void onDraw(Canvas c){
      int w=getWidth(),h=getHeight();ground=h*.78f;buttons.clear();actions.clear();
      if(page.equals("home"))drawHome(c,w,h);
      else if(page.equals("levels"))drawLevels(c,w,h);
      else if(page.equals("skins"))drawSkins(c,w,h);
      else if(page.equals("achievements"))drawAchievements(c,w,h);
      else if(page.equals("creator"))drawCreator(c,w,h);
      else if(page.equals("game"))drawGame(c,w,h);
      postInvalidateDelayed(page.equals("game")?16:250);
    }
    void drawHome(Canvas c,int w,int h){
      background(c,w,h,bg);center(c,"GEOMETRY BOX",w/2f,h*.25f,Math.min(58,w/11),Color.WHITE);
      center(c,"ПРЫГАЙ • СОЗДАВАЙ • ПОБЕЖДАЙ",w/2f,h*.34f,18,accent);
      p.setColor(skinColors[skin]);c.save();c.rotate(12,w/2f,h*.46f);c.drawRoundRect(w/2f-27,h*.46f-27,w/2f+27,h*.46f+27,8,8,p);c.restore();
      button(c,w*.35f,h*.55f,w*.30f,48,"ИГРАТЬ","levels",Color.rgb(0,145,190));
      button(c,w*.08f,h*.68f,w*.25f,46,"КРЕАТОР","creator",Color.rgb(110,65,190));
      button(c,w*.375f,h*.68f,w*.25f,46,"СКИНЫ","skins",Color.rgb(190,90,45));
      button(c,w*.67f,h*.68f,w*.25f,46,"ДОСТИЖЕНИЯ","achievements",Color.rgb(35,145,90));
      text(c,"Рекорд: "+best+"     Победы: "+wins,w*.36f,h*.91f,18,Color.LTGRAY);
    }
    void header(Canvas c,String title,int w){text(c,title,28,42,30,Color.WHITE);button(c,w-112,16,84,38,"НАЗАД","home",Color.DKGRAY);}
    void drawLevels(Canvas c,int w,int h){
      background(c,w,h,bg);header(c,"ВЫБЕРИ УРОВЕНЬ",w);
      for(int i=0;i<3;i++){float x=w*.12f+i*w*.25f;p.setColor(levelColors[i]);c.drawRoundRect(x,h*.28f,x+w*.19f,h*.67f,20,20,p);center(c,""+(i+1),x+w*.095f,h*.43f,48,Color.WHITE);center(c,levelNames[i],x+w*.095f,h*.53f,18,Color.WHITE);center(c,levelDiff[i],x+w*.095f,h*.60f,16,Color.WHITE);buttons.add(new RectF(x,h*.28f,x+w*.19f,h*.67f));actions.add("level"+i);}
      button(c,w*.39f,h*.76f,w*.22f,42,"МОЙ УРОВЕНЬ","custom",Color.rgb(105,65,175));
    }
    void drawSkins(Canvas c,int w,int h){
      background(c,w,h,bg);header(c,"ВЫБЕРИ СКИН",w);
      for(int i=0;i<skinColors.length;i++){int col=i%3,row=i/3;float x=w*.22f+col*w*.20f,y=h*.25f+row*h*.31f;p.setColor(skinColors[i]);c.drawRoundRect(x,y,x+55,y+55,10,10,p);center(c,skinNames[i],x+28,y+82,17,Color.WHITE);String status=(i==0||wins>=i*2)?"ВЫБРАТЬ":"НУЖНО "+(i*2)+" ПОБЕД";button(c,x+70,y+8,130,40,status,"skin"+i, i==skin?Color.rgb(25,140,90):Color.rgb(65,75,110));}
    }
    void drawAchievements(Canvas c,int w,int h){
      background(c,w,h,bg);header(c,"ДОСТИЖЕНИЯ",w);
      String[] names={"Первый прыжок","Первая победа","Пять побед","Мастер прыжков","Создатель"};
      String[] desc={"Совершить прыжок","Пройти уровень","Победить 5 раз","Сделать 50 прыжков","Сохранить свой уровень"};
      boolean[] ok={jumps>0,wins>0,wins>=5,jumps>=50,saved>0};
      for(int i=0;i<5;i++){float y=h*.18f+i*h*.145f;p.setColor(ok[i]?Color.rgb(25,120,90):Color.rgb(55,60,85));c.drawRoundRect(w*.17f,y,w*.83f,y+48,12,12,p);text(c,(ok[i]?"★ ":"☆ ")+names[i],w*.20f,y+21,18,Color.WHITE);text(c,desc[i],w*.20f,y+40,13,Color.LTGRAY);}
    }
    void drawCreator(Canvas c,int w,int h){
      background(c,w,h,Color.rgb(15,18,45));header(c,"КРЕАТОР УРОВНЕЙ",w);
      text(c,"Выбери объект и нажми на клетку",28,76,17,Color.LTGRAY);
      float left=w*.08f,top=h*.17f,cw=w*.84f/12,ch=h*.48f/5;
      for(int yy=0;yy<5;yy++)for(int xx=0;xx<12;xx++){int idx=yy*12+xx;p.setColor(Color.rgb(40,48,80));p.setStyle(Paint.Style.STROKE);c.drawRect(left+xx*cw,top+yy*ch,left+(xx+1)*cw,top+(yy+1)*ch,p);p.setStyle(Paint.Style.FILL);if(idx<creatorBlocks.size()&&creatorBlocks.get(idx)!=0){p.setColor(creatorBlocks.get(idx)==1?accent:Color.rgb(255,65,125));c.drawRect(left+xx*cw+3,top+yy*ch+3,left+(xx+1)*cw-3,top+(yy+1)*ch-3,p);}}
      button(c,w*.10f,h*.72f,w*.18f,40,"ПЛАТФОРМА","palette1",Color.rgb(0,130,170));
      button(c,w*.31f,h*.72f,w*.18f,40,"ШИП","palette2",Color.rgb(175,55,100));
      button(c,w*.52f,h*.72f,w*.18f,40,"ЛАСТИК","palette0",Color.rgb(80,85,110));
      button(c,w*.73f,h*.72f,w*.18f,40,"СОХРАНИТЬ","savecreator",Color.rgb(30,145,85));
      text(c,"Объектов: "+countBlocks(),w*.42f,h*.91f,17,Color.WHITE);
    }
    int countBlocks(){int n=0;for(int v:creatorBlocks)if(v!=0)n++;return n;}
    void drawGame(Canvas c,int w,int h){
      int color=custom?Color.rgb(20,20,55):levelColors[selectedLevel];background(c,w,h,color);
      if(playing&&!dead&&!won){long now=System.nanoTime();float dt=lastTime==0?0:Math.min(.04f,(now-lastTime)/1e9f);lastTime=now;velocity+=1900*dt;playerY+=velocity*dt;distance+=speed*dt*60;obstacleX-=speed*dt*60;if(playerY>0){playerY=0;velocity=0;}
        if(obstacleX<-60){obstacleX=w+random.nextInt(170)+100;score++;if(score>best){best=score;save();}}
        float py=ground-42+playerY;
        if(obstacleX<145&&obstacleX+48>90&&py+42>ground-44)dead=true;
        if(distance>2600){won=true;playing=false;wins++;save();}
      }
      p.setColor(accent);p.setStrokeWidth(4);c.drawLine(0,ground,w,ground,p);
      if(custom){for(int i=0;i<creatorBlocks.size();i++){if(creatorBlocks.get(i)==2){float ox=w*.22f+i%12*(w*.84f/12);p.setColor(Color.MAGENTA);Path tri=new Path();tri.moveTo(ox,ground);tri.lineTo(ox+22,ground-42);tri.lineTo(ox+44,ground);tri.close();c.drawPath(tri,p);}}}
      else{p.setColor(Color.rgb(255,65,125));Path t=new Path();t.moveTo(obstacleX,ground);t.lineTo(obstacleX+24,ground-44);t.lineTo(obstacleX+48,ground);t.close();c.drawPath(t,p);}
      float py=ground-42+playerY;p.setColor(skinColors[skin]);c.drawRoundRect(90,py,132,py+42,7,7,p);
      text(c,custom?"МОЙ УРОВЕНЬ":levelNames[selectedLevel],24,34,19,Color.WHITE);text(c,"СЧЁТ: "+score,w-130,34,19,Color.WHITE);
      if(!playing&&!dead&&!won)center(c,"НАЖМИ, ЧТОБЫ НАЧАТЬ",w/2f,h*.45f,25,Color.WHITE);
      if(dead){center(c,"ПОПРОБУЙ ЕЩЁ!",w/2f,h*.43f,32,Color.WHITE);button(c,w*.38f,h*.55f,w*.24f,46,"ЗАНОВО","restart",Color.rgb(180,55,85));button(c,w*.38f,h*.67f,w*.24f,42,"В МЕНЮ","home",Color.DKGRAY);}
      if(won){center(c,"УРОВЕНЬ ПРОЙДЕН!",w/2f,h*.43f,32,Color.WHITE);button(c,w*.38f,h*.57f,w*.24f,46,"ДАЛЬШЕ","next",Color.rgb(25,145,90));button(c,w*.38f,h*.69f,w*.24f,42,"В МЕНЮ","home",Color.DKGRAY);}
    }
    void start(boolean isCustom){custom=isCustom;page="game";playing=false;dead=false;won=false;score=0;distance=0;speed=custom?9:8+selectedLevel*2;obstacleX=getWidth()+160;playerY=0;velocity=0;lastTime=0;attempts++;save();}
    void jump(){if(!playing){playing=true;lastTime=0;}if(playerY==0){velocity=-690;jumps++;save();}}
    void tap(float x,float y){if(page.equals("game")){if(dead||won){for(int i=0;i<buttons.size();i++)if(buttons.get(i).contains(x,y)){act(actions.get(i));return;}}else jump();return;}
      if(page.equals("creator")){float left=getWidth()*.08f,top=getHeight()*.17f,cw=getWidth()*.84f/12,ch=getHeight()*.48f/5;if(y>=top&&y<top+ch*5&&x>=left&&x<left+cw*12){int xx=(int)((x-left)/cw),yy=(int)((y-top)/ch),idx=yy*12+xx;while(creatorBlocks.size()<60)creatorBlocks.add(0);creatorBlocks.set(idx,palette);invalidate();return;}}
      for(int i=buttons.size()-1;i>=0;i--)if(buttons.get(i).contains(x,y)){act(actions.get(i));return;}
    }
    void act(String a){
      if(a.equals("home")){page="home";}
      else if(a.equals("levels"))page="levels";
      else if(a.equals("skins"))page="skins";
      else if(a.equals("achievements"))page="achievements";
      else if(a.equals("creator"))page="creator";
      else if(a.startsWith("level")){selectedLevel=Integer.parseInt(a.substring(5));start(false);}
      else if(a.equals("custom"))start(true);
      else if(a.startsWith("skin")){int s=Integer.parseInt(a.substring(4));if(s==0||wins>=s*2){skin=s;save();}}
      else if(a.startsWith("palette"))palette=Integer.parseInt(a.substring(7));
      else if(a.equals("savecreator")){StringBuilder b=new StringBuilder();for(int v:creatorBlocks){if(b.length()>0)b.append(',');b.append(v);}prefs.edit().putString("creator",b.toString()).apply();saved++;save();page="home";}
      else if(a.equals("restart"))start(custom);
      else if(a.equals("next")){selectedLevel=(selectedLevel+1)%3;start(false);}
      invalidate();
    }
    @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){tap(e.getX(),e.getY());invalidate();return true;}return true;}
  }
}
