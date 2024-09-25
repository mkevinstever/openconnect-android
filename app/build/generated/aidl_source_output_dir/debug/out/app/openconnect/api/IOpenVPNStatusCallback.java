/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package app.openconnect.api;
/**
 * Example of a callback interface used by IRemoteService to send
 * synchronous notifications back to its clients.  Note that this is a
 * one-way interface so the server does not block waiting for the client.
 */
public interface IOpenVPNStatusCallback extends android.os.IInterface
{
  /** Default implementation for IOpenVPNStatusCallback. */
  public static class Default implements app.openconnect.api.IOpenVPNStatusCallback
  {
    /** Called when the service has a new status for you. */
    @Override public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements app.openconnect.api.IOpenVPNStatusCallback
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an app.openconnect.api.IOpenVPNStatusCallback interface,
     * generating a proxy if needed.
     */
    public static app.openconnect.api.IOpenVPNStatusCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof app.openconnect.api.IOpenVPNStatusCallback))) {
        return ((app.openconnect.api.IOpenVPNStatusCallback)iin);
      }
      return new app.openconnect.api.IOpenVPNStatusCallback.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_newStatus:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          java.lang.String _arg3;
          _arg3 = data.readString();
          this.newStatus(_arg0, _arg1, _arg2, _arg3);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements app.openconnect.api.IOpenVPNStatusCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /** Called when the service has a new status for you. */
      @Override public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(uuid);
          _data.writeString(state);
          _data.writeString(message);
          _data.writeString(level);
          boolean _status = mRemote.transact(Stub.TRANSACTION_newStatus, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_newStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  public static final java.lang.String DESCRIPTOR = "app.openconnect.api.IOpenVPNStatusCallback";
  /** Called when the service has a new status for you. */
  public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException;
}
