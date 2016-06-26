/**
   Bitbang example of 9600 baud serial data

   Protocol
   - 2x stop bits
   - pull low
   - start bit
   - send bit
   - wait
   - repeat send bit for each char
   - set high

*/

#define TX 2
#define BIT_T 104 // (1/9600 baud) = 104

const char* str = "hello akka!";


void setup() {
  pinMode(TX, OUTPUT);
  digitalWrite(TX, HIGH);
}

void loop() {
  const char* strp = str;
  for (int i = 0; str[i]; ++i) txch(str[i]);
  delay(1000);
}

void txch(const char& ch)
{
  delayMicroseconds(BIT_T * 2);
  digitalWrite(TX, LOW);
  delayMicroseconds(BIT_T);
  for (int i = 0; i < 8; ++i)
  {
    digitalWrite(TX, bitRead(ch, i));
    delayMicroseconds(BIT_T);
  }
  digitalWrite(TX, HIGH);
}

