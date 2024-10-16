/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package app.openconnect.api;
public interface IOpenVPNAPIService extends android.os.IInterface
{
  /** Default implementation for IOpenVPNAPIService. */
  public static class Default implements app.openconnect.api.IOpenVPNAPIService
  {
    @Override public java.util.List<app.openconnect.api.APIVpnProfile> getProfiles() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void startProfile(java.lang.String profileUUID) throws android.os.RemoteException
    {
    }
    /** Use a profile with all certificates etc. embedded */
    @Override public boolean addVPNProfile(java.lang.String name, java.lang.String config) throws android.os.RemoteException
    {
      return false;
    }
    /** start a profile using an config */
    @Override public void startVPN(java.lang.String inlineconfig) throws android.os.RemoteException
    {
    }
    /**
     * This permission framework is used  to avoid confused deputy style attack to the VPN
     * calling this will give null if the app is allowed to use the external API and an Intent
     * that can be launched to request permissions otherwise
     */
    @Override public android.content.Intent prepare(java.lang.String packagename) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Used to trigger to the Android VPN permission dialog (VPNService.prepare()) in advance,
     * if this return null OpenVPN for ANdroid already has the permissions otherwise you can start the returned Intent
     * to let OpenVPN for Android request the permission
     */
    @Override public android.content.Intent prepareVPNService() throws android.os.RemoteException
    {
      return null;
    }
    /** Disconnect the VPN */
    @Override public void disconnect() throws android.os.RemoteException
    {
    }
    /** Registers to receive OpenVPN Status Updates */
    @Override public void registerStatusCallback(app.openconnect.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException
    {
    }
    /** Remove a previously registered callback interface. */
    @Override public void unregisterStatusCallback(app.openconnect.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements app.openconnect.api.IOpenVPNAPIService
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an app.openconnect.api.IOpenVPNAPIService interface,
     * generating a proxy if needed.
     */
    public static app.openconnect.api.IOpenVPNAPIService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof app.openconnect.api.IOpenVPNAPIService))) {
        return ((app.openconnect.api.IOpenVPNAPIService)iin);
      }
      return new app.openconnect.api.IOpenVPNAPIService.Stub.Proxy(obj);
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
        case TRANSACTION_getProfiles:
        {
          java.util.List<app.openconnect.api.APIVpnProfile> _result = this.getProfiles();
          reply.writeNoException();
          _Parcel.writeTypedList(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_startProfile:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.startProfile(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_addVPNProfile:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          boolean _result = this.addVPNProfile(_arg0, _arg1);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_startVPN:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.startVPN(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_prepare:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.content.Intent _result = this.prepare(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_prepareVPNService:
        {
          android.content.Intent _result = this.prepareVPNService();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_disconnect:
        {
          this.disconnect();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_registerStatusCallback:
        {
          app.openconnect.api.IOpenVPNStatusCallback _arg0;
          _arg0 = app.openconnect.api.IOpenVPNStatusCallback.Stub.asInterface(data.readStrongBinder());
          this.registerStatusCallback(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_unregisterStatusCallback:
        {
          app.openconnect.api.IOpenVPNStatusCallback _arg0;
          _arg0 = app.openconnect.api.IOpenVPNStatusCallback.Stub.asInterface(data.readStrongBinder());
          this.unregisterStatusCallback(_arg0);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements app.openconnect.api.IOpenVPNAPIService
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
      @Override public java.util.List<app.openconnect.api.APIVpnProfile> getProfiles() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.util.List<app.openconnect.api.APIVpnProfile> _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getProfiles, _data, _reply, 0);
          _reply.readException();
          _result = _reply.createTypedArrayList(app.openconnect.api.APIVpnProfile.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void startProfile(java.lang.String profileUUID) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(profileUUID);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startProfile, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Use a profile with all certificates etc. embedded */
      @Override public boolean addVPNProfile(java.lang.String name, java.lang.String config) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(name);
          _data.writeString(config);
          boolean _status = mRemote.transact(Stub.TRANSACTION_addVPNProfile, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** start a profile using an config */
      @Override public void startVPN(java.lang.String inlineconfig) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(inlineconfig);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startVPN, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * This permission framework is used  to avoid confused deputy style attack to the VPN
       * calling this will give null if the app is allowed to use the external API and an Intent
       * that can be launched to request permissions otherwise
       */
      @Override public android.content.Intent prepare(java.lang.String packagename) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.content.Intent _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packagename);
          boolean _status = mRemote.transact(Stub.TRANSACTION_prepare, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.content.Intent.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Used to trigger to the Android VPN permission dialog (VPNService.prepare()) in advance,
       * if this return null OpenVPN for ANdroid already has the permissions otherwise you can start the returned Intent
       * to let OpenVPN for Android request the permission
       */
      @Override public android.content.Intent prepareVPNService() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.content.Intent _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_prepareVPNService, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.content.Intent.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Disconnect the VPN */
      @Override public void disconnect() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Registers to receive OpenVPN Status Updates */
      @Override public void registerStatusCallback(app.openconnect.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(cb);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerStatusCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Remove a previously registered callback interface. */
      @Override public void unregisterStatusCallback(app.openconnect.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(cb);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterStatusCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_getProfiles = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_startProfile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_addVPNProfile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_startVPN = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_prepare = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_prepareVPNService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_disconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_registerStatusCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_unregisterStatusCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
  }
  public static final java.lang.String DESCRIPTOR = "app.openconnect.api.IOpenVPNAPIService";
  public java.util.List<app.openconnect.api.APIVpnProfile> getProfiles() throws android.os.RemoteException;
  public void startProfile(java.lang.String profileUUID) throws android.os.RemoteException;
  /** Use a profile with all certificates etc. embedded */
  public boolean addVPNProfile(java.lang.String name, java.lang.String config) throws android.os.RemoteException;
  /** start a profile using an config */
  public void startVPN(java.lang.String inlineconfig) throws android.os.RemoteException;
  /**
   * This permission framework is used  to avoid confused deputy style attack to the VPN
   * calling this will give null if the app is allowed to use the external API and an Intent
   * that can be launched to request permissions otherwise
   */
  public android.content.Intent prepare(java.lang.String packagename) throws android.os.RemoteException;
  /**
   * Used to trigger to the Android VPN permission dialog (VPNService.prepare()) in advance,
   * if this return null OpenVPN for ANdroid already has the permissions otherwise you can start the returned Intent
   * to let OpenVPN for Android request the permission
   */
  public android.content.Intent prepareVPNService() throws android.os.RemoteException;
  /** Disconnect the VPN */
  public void disconnect() throws android.os.RemoteException;
  /** Registers to receive OpenVPN Status Updates */
  public void registerStatusCallback(app.openconnect.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException;
  /** Remove a previously registered callback interface. */
  public void unregisterStatusCallback(app.openconnect.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedList(
        android.os.Parcel parcel, java.util.List<T> value, int parcelableFlags) {
      if (value == null) {
        parcel.writeInt(-1);
      } else {
        int N = value.size();
        int i = 0;
        parcel.writeInt(N);
        while (i < N) {
    writeTypedObject(parcel, value.get(i), parcelableFlags);
          i++;
        }
      }
    }
  }
}
