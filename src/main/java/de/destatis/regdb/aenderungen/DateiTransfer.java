package de.destatis.regdb.aenderungen;

import java.nio.file.Path;

public class DateiTransfer
{
  private Path quellPfad;
  private String zielDateiname;
  private String zielVerzeichnis;
  private boolean transferErolgreich;

  public DateiTransfer()
  {
    this(null, null, null);
  }

  public DateiTransfer(Path quellPfad, String zielDateiname)
  {
    this(quellPfad, zielDateiname, null);
  }

  public DateiTransfer(Path quellPfad, String zielDateiname, String zielVerzeichnis)
  {
    this.quellPfad = quellPfad;
    this.zielDateiname = zielDateiname;
    this.zielVerzeichnis = zielVerzeichnis;
    this.transferErolgreich = false;
  }

  public Path getQuellPfad()
  {
    return this.quellPfad;
  }

  public void setQuellPfad(Path quellPfad)
  {
    this.quellPfad = quellPfad;
  }

  public String getZielDateiname()
  {
    return this.zielDateiname;
  }

  public void setZielDateiname(String zielDateiname)
  {
    this.zielDateiname = zielDateiname;
  }

  public String getZielVerzeichnis()
  {
    return this.zielVerzeichnis;
  }

  public void setZielVerzeichnis(String zielVerzeichnis)
  {
    this.zielVerzeichnis = zielVerzeichnis;
  }

  public boolean isTransferErolgreich()
  {
    return this.transferErolgreich;
  }

  public void setTransferErolgreich(boolean transferErolgreich)
  {
    this.transferErolgreich = transferErolgreich;
  }
}
