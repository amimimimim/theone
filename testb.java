
package routing;

import java.util.ArrayList;
import java.util.List;
import util.Tuple;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimError;
import routing.util.EnergyModel;

import core.Coord;
import core.SimClock;

public class testb extends ActiveRouter {

  // 1 is ferry, 0 is smartphone device
  //public static int countA = 0;
  //public static int countB = 0;
  public static boolean ferryflag;
  public static boolean subfrrryflag;
  public static int ferryScore;
  public static int borderScore;
  public static int transflag;
  public static int count;
  public static double e;
  public static double S;
  public static int c;
  public static EnergyModel energy;
  public static final String PRO_MESSAGE_FERRY_ROUTER_NS = "ProMessageFerryRouter.";
  public static final String FERRY_FLAG_S = "ferryflag";

  public testb (Settings s) {
    super(s);
    ferryflag = s.getBoolean(PRO_MESSAGE_FERRY_ROUTER_NS + FERRY_FLAG_S);
    super.setFerryFlag(ferryflag);
    this.ferryScore = 0;
    if (ferryflag) {
      this.ferryScore = Integer.MAX_VALUE;
      this.transflag = Integer.MAX_VALUE;
    }
    this.borderScore = 0;
    super.setFerryScore(ferryScore);
    this.transflag = 0;
    super.setTransFlag(transflag);
    this.e = getEnergy();
    super.setEnergy(e);
    this.S = e * 0.5;
    super.setS(S);
    this.count = 0;
    super.setCount(count);
    this.c = 2;
    super.setC(c);
  }

  protected testb(testb r) {
    super(r);

    this.ferryflag = r.ferryflag;
    super.setFerryFlag(ferryflag);
    this.ferryScore = r.ferryScore;
    this.borderScore = r.borderScore;
    super.setFerryScore(ferryScore);
    this.transflag = r.transflag;
    super.setTransFlag(transflag);
    this.e = r.e;
    super.setEnergy(e);
    this.S = r.S;
    this.count = r.count;
    this.c = r.c;
    super.setC(c);
    //super.setS(S);
  }

  @Override
  protected int startTransfer(Message m, Connection con) {
    DTNHost thisHost = this.getHost();
    MessageRouter thisMr = thisHost.getRouter();
    DTNHost otherHost = con.getOtherNode(this.getHost());
    MessageRouter otherMr = otherHost.getRouter();
    if (otherHost.isMovementActive()) {
      if (otherMr.getFerryFlag() &&
      otherMr.getFerryScore() > thisMr.getFerryScore()) {
        int retVal = super.startTransfer(m, con);
        if (retVal == RCV_OK) {
          List<DTNHost> hops = m.getHops();

          if(this.S > this.e){
            for (int i = 0; i < hops.size(); i++) {
            DTNHost thisFerry = hops.get(i);
            MessageRouter thisFe = thisFerry.getRouter();
            if(0.5 * this.S > this.e){
              this.count++;
              super.setCount(this.count);
              this.S = this.S / this.count;
              super.setS(this.S);
              this.ferryScore = thisFe.getFerryScore();
              for(int j = 2; this.ferryScore > 0.5 * this.S; j++){
                this.ferryScore = this.ferryScore / j;
                thisFe.setFerryScore(this.ferryScore);
              }
              this.c = super.getC();
              this.c = 0;
              super.setC(this.c);
            }else{
              this.ferryScore = thisFe.getFerryScore();
              this.ferryScore -= 2;
                this.ferryScore = thisFe.getFerryScore();
                this.ferryScore += (i + 1);
                thisFe.setFerryScore(this.ferryScore);
                this.c = super.getC();
              this.c++;
              super.setC(this.c);
              this.count = getC();
              this.count++;
              super.setCount(this.count);
              if(this.c > 5){
                for (int k = this.count; this.ferryScore > this.S ;k++ ) {
                  this.count++;
                  super.setCount(this.count);
                  this.S = this.S / this.count;
                }
                super.setCount(this.count);
                super.setS(this.S);
                this.c = 0;
                setC(this.c);
              }
            }
            }
            return retVal;
          }

          for (int i = 0; i < hops.size(); i++) {
            DTNHost thisFerry = hops.get(i);
            MessageRouter thisFe = thisFerry.getRouter();
            this.ferryScore = thisFe.getFerryScore();
            this.ferryScore += (i + 1);
            thisFe.setFerryScore(this.ferryScore);
          }
          this.c++;
          super.setC(this.c);
        }
        return retVal;
      }
      if (!otherMr.getFerryFlag() &&
        otherMr.getFerryScore() > (thisMr.getFerryScore())) {
          // check
          if ((otherMr.getFerryScore() > thisMr.getFerryScore()) &&
          (this.S > this.e)) {
          }
          return super.startTransfer(m, con);
        }
    }
    return 1;
  }
  //public boolean transferFlag;
  @Override
  protected Tuple<Message, Connection> tryMessagesForConnected(
    List<Tuple<Message, Connection>> tuples) {
   if (tuples.size() == 0) {
     return null;
   }
   for (Tuple<Message, Connection> t : tuples) {
     Message m = t.getKey();
     Connection con = t.getValue();
     if (startTransfer(m, con) == RCV_OK) {
       return t;
     }
   }
   return null;
  }

  @Override
  protected Connection tryMessagesToConnections(List<Message> messages,
    List<Connection> connections) {
  for (int i = 0, n = connections.size(); i < n; i++) {
    Connection con = connections.get(i);
    Message started = super.tryAllMessages(con, messages);
    if (started != null) {
      return con;
    }
  }
  return null;
 }

 @Override
 protected Connection exchangeDeliverableMessages() {
   List<Connection> connections = super.getConnections();
   if (connections.size() == 0) {
     return null;
   }
   @SuppressWarnings(value = "unchecked")
   Tuple<Message, Connection> t =
    tryMessagesForConnected(super.sortByQueueMode(
      super.getMessagesForConnected()));
  if (t != null) {

    //if (transferFlag) {
    //  this.ferryScore = this.getHost().getRouter().getFerryScore();
    //  this.ferryScore++;
    //  System.out.println("ScoreUptest:" + this.ferryScore);
    //  this.getHost().getRouter().setFerryScore(this.ferryScore);
    //}

    return t.getValue();
  }
  for (Connection con : connections) {
    if (con.getOtherNode(getHost()).requestDeliverableMessages(con)) {
      return con;
    }
  }
  return null;
  }

  @Override
  public void update() {
    super.update();
    if (isTransferring() || !canStartTransfer()) {
      return;
    }
    if (exchangeDeliverableMessages() != null) {
      return;
    }
    this.tryAllMessagesToAllConnections();
  }

  @Override
  public testb replicate() {
    return new testb(this);
  }
}
