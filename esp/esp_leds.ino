#include "Config.h"
#include "WIFI.h"
#include "Server.h"
#include "leds.h"

byte a[363];
int length = NUM_LEDS * 3;
int i = 0;

void setup(void){
  Serial.begin(9600);
  pinMode(led, OUTPUT);
  for(int i=0; i< 3; i++) {
    digitalWrite(led, !digitalRead(led));
    delay(500);
  }
  leds_init();
  //WIFI_init(false);
  //server_init();;
}

void loop(void){
  //server.handleClient();

  //leds_test();

  while(Serial.available() > 0){
      a[i] = Serial.read(); 
      i++;
      if (i >= 363) {
        i = 0;
        set_leds_bytes(a, length);
      }
      //Serial.print(a);
    }
                     
}
