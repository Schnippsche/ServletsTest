package de.destatis.regdb;

public class TransferException extends Exception
{

  public TransferException()
  {
    super();
  }

  public TransferException(String message)
  {
    super(message);
  }

  public TransferException(Throwable cause)
  {
    super(cause);
  }

  public TransferException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
