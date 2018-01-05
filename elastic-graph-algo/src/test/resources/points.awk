BEGIN{
  OFS = ","
  print "CASE_ID","X","Y"
  XMIN = -150.0
  XMAX = 150.0
  YMIN = -50.0
  YMAX = 50.0
  DX = XMAX - XMIN
  DY = YMAX - YMIN
  srand()
  for(I=0;I<10;I++){
    XM = XMIN + rand() * DX
    YM = YMIN + rand() * DY
    GID = "G" + I
    JMAX=5+int(30*rand())
    for(J=0;J<JMAX;J++){
        AX = rand()
        BX = rand()
        if(rand() < 0.5) AX = AX * -1.0
        if(rand() < 0.5) BX = BX * -1.0
        print GID,XM+AX,YM+BX
    }
  }
}
