// Six quiet landscape panels. Keep broad silhouettes and generous open space;
// do not add survey grids, settlements, small labels, or procedural detail.
// Run from any directory: node frontend/scripts/generate-panorama-maps.mjs
import { mkdirSync, writeFileSync } from "node:fs";
const output = new URL("../src/assets/panoramas/", import.meta.url);
mkdirSync(output, { recursive: true });

const shape = (d, fill, extra = "") =>
  `<path d="${d}" fill="${fill}" ${extra}/>`;
const line = (d, opacity = 0.24, width = 1.6) =>
  `<path d="${d}" fill="none" stroke="#e0d4ae" stroke-opacity="${opacity}" stroke-width="${width}" stroke-linecap="round" stroke-linejoin="round"/>`;
const trees = (items, color) =>
  `<g fill="${color}">${items
    .map(
      ([x, y, h]) =>
        `<path transform="translate(${x} ${y}) scale(${h / 100})" d="M0-100C-5-77-17-61-22-48L-14-50-34-18-22-22-43 8Q0-1 43 8L22-22 34-18 14-50 22-48C17-61 5-77 0-100Z"/>`,
    )
    .join("")}</g>`;

function plate(id, title, description, colors, body) {
  const [ground, light, dark] = colors;
  writeFileSync(
    new URL(`${id}.svg`, output),
    `<svg xmlns="http://www.w3.org/2000/svg" width="1800" height="1100" viewBox="0 0 1800 1100">
<title>${title} — an Archivum landscape</title>
<desc>${description} Fictional decorative artwork, not world records.</desc>
<defs>
  <linearGradient id="ground" x2=".8" y2="1"><stop stop-color="${light}"/><stop offset="1" stop-color="${ground}"/></linearGradient>
  <linearGradient id="wash" x2="0" y2="1"><stop stop-color="${light}" stop-opacity=".18"/><stop offset=".55" stop-color="${ground}" stop-opacity="0"/><stop offset="1" stop-color="${dark}" stop-opacity=".3"/></linearGradient>
  <radialGradient id="mist"><stop stop-color="#dedac1" stop-opacity=".18"/><stop offset="1" stop-color="#dedac1" stop-opacity="0"/></radialGradient>
  <filter id="paper" x="0" y="0" width="100%" height="100%"><feTurbulence type="fractalNoise" baseFrequency=".65" numOctaves="3" stitchTiles="stitch"/><feColorMatrix type="saturate" values="0"/><feComponentTransfer><feFuncA type="linear" slope=".055"/></feComponentTransfer><feBlend in="SourceGraphic" mode="soft-light"/></filter>
</defs>
<rect width="1800" height="1100" fill="url(#ground)"/>
${body}
<rect width="1800" height="1100" fill="url(#wash)"/>
<ellipse cx="1000" cy="380" rx="1050" ry="350" fill="url(#mist)"/>
<rect width="1800" height="1100" fill="transparent" filter="url(#paper)"/>
</svg>\n`,
  );
}

// I. Woodland: a pale clearing between large, overlapping forest masses.
plate(
  "green-reach",
  "The Green Reach",
  "Deep evergreen woodland around a still ribbon of water, with distant ridges and a few fine terrain lines.",
  ["#68816d", "#a4ad8a", "#193e36"],
  shape("M0 225Q270 90 545 235T1110 205T1800 180V1100H0Z", "#7c917c") +
    shape("M0 400Q260 205 550 375T1130 345T1800 300V1100H0Z", "#637f6b") +
    shape(
      "M0 555Q290 420 520 515C770 610 910 365 1180 360Q1500 325 1800 510V1100H0Z",
      "#97a383",
    ) +
    shape(
      "M1100 360C910 500 1150 545 1025 675S725 900 840 1100H1080C890 900 1250 775 1190 620S980 480 1190 360Z",
      "#5f8680",
    ) +
    line("M1100 360C910 500 1150 545 1025 675S725 900 840 1100", 0.4, 2.5) +
    shape(
      "M0 460Q190 350 365 460T710 600Q785 700 680 810T710 1100H0Z",
      "#365f4b",
    ) +
    shape(
      "M1240 360Q1530 250 1800 395V1100H1090Q1030 975 1200 840T1260 605Q1150 455 1240 360Z",
      "#406951",
    ) +
    trees(
      [
        [1260, 470, 110],
        [1340, 440, 155],
        [1430, 423, 115],
        [1515, 407, 155],
        [1630, 420, 180],
        [1745, 453, 130],
        [1190, 595, 135],
        [1330, 590, 180],
        [1490, 555, 160],
        [1660, 595, 200],
        [1770, 665, 170],
      ],
      "#345b48",
    ) +
    trees(
      [
        [25, 600, 160],
        [130, 560, 130],
        [260, 570, 180],
        [385, 620, 140],
        [500, 690, 155],
        [600, 770, 110],
      ],
      "#2c5443",
    ) +
    line(
      "M1330 705Q1520 595 1790 745M1320 742Q1530 645 1740 766M60 768Q225 715 390 790M105 807Q250 765 380 828",
      0.17,
    ) +
    shape("M0 985Q260 810 520 955T990 1060T1800 985V1100H0Z", "#294f40"),
);

// II. Mountains: one sawtooth ridge, broad shadow facets, mist below.
plate(
  "quiet-peaks",
  "The Quiet Peaks",
  "A slate mountain range with parchment-lit summits above a quiet, mist-filled valley.",
  ["#728582", "#aab1a0", "#2f4a48"],
  shape(
    "M0 490L190 315 320 390 555 210 720 375 940 280 1150 400 1440 190 1660 365 1800 310V1100H0Z",
    "#879991",
  ) +
    shape(
      "M0 700L230 455 370 515 610 315 795 550 1060 230 1190 385 1370 125 1600 430 1695 355 1800 510V1100H0Z",
      "#586f6c",
    ) +
    shape(
      "M610 315L570 520 795 550ZM1060 230L988 545 1190 385ZM1370 125L1300 515 1600 430Z",
      "#8e9d91",
    ) +
    shape(
      "M610 315L545 405 598 380 625 416 653 370ZM1060 230L989 330 1049 306 1070 347 1112 294ZM1370 125L1275 270 1344 229 1382 266 1423 207Z",
      "#c2c4ae",
    ) +
    line(
      "M1370 286L1335 463 1430 592M1080 400L1130 497 1060 623M603 461L655 527 650 639",
      0.29,
    ) +
    shape(
      "M0 840Q270 640 480 720T900 702T1350 675T1800 715V1100H0Z",
      "#82958b",
    ) +
    shape("M0 955Q270 785 570 850T1130 812T1800 905V1100H0Z", "#657f75") +
    line("M950 951Q1210 790 1660 944M1090 960Q1350 869 1590 968", 0.2) +
    shape(
      "M0 1000Q270 915 485 997T1000 1070T1490 1010T1800 1020V1100H0Z",
      "#3e6156",
    ),
);

// III. Archipelago: five asymmetric landforms with uninterrupted sea between.
const islands = [
  "M230 300C255 238 339 214 395 239S450 314 410 338 290 380 250 350Z",
  "M690 470C697 392 757 365 817 345S889 281 944 311 973 375 1024 404 1135 463 1113 522 1010 540 960 581 848 606 823 554 715 547 690 470Z",
  "M1200 215C1250 168 1315 190 1338 226S1460 265 1440 307 1326 325 1282 295 1180 271 1200 215Z",
  "M1205 778C1286 696 1330 664 1401 698S1490 719 1537 770 1640 854 1583 897 1482 885 1420 909 1278 887 1260 842 1177 831 1205 778Z",
  "M488 797C475 750 511 706 553 729S615 820 598 855 536 903 512 867Z",
];
plate(
  "lantern-isles",
  "The Lantern Isles",
  "Five warm stone islands suspended in open teal water, traced by soft shallows and a handful of ridge lines.",
  ["#426e70", "#74958c", "#244c52"],
  islands
    .map((d) =>
      shape(
        d,
        "none",
        'stroke="#aec0a2" stroke-opacity=".12" stroke-width="48" stroke-linejoin="round"',
      ),
    )
    .join("") +
    islands
      .map((d) =>
        shape(
          d,
          "#a6aa83",
          'stroke="#d4cba3" stroke-opacity=".7" stroke-width="2"',
        ),
      )
      .join("") +
    shape(
      "M740 466Q810 435 865 367L918 415 983 425 1047 485 960 477 900 533 843 483Z",
      "#768e70",
    ) +
    shape(
      "M1270 790L1370 735 1421 788 1538 841 1414 821 1370 848Z",
      "#788e72",
    ) +
    line(
      "M776 485L863 411 900 451 958 451 1012 494M1293 805L1369 768 1420 808 1490 839",
      0.55,
    ) +
    line(
      "M1090 651Q1290 555 1510 622M1130 675Q1320 596 1460 641M180 537Q360 483 511 527M228 565Q382 528 473 549",
      0.17,
    ),
);

// IV. Coast: one sweeping shoreline, open water and a broad headland.
const coast =
  "M0 0H1800V272C1580 235 1535 360 1340 410S1100 421 1060 555 1250 741 1120 837 788 800 679 940 527 1040 510 1100H0Z";
plate(
  "amber-coast",
  "The Amber Coast",
  "A long, sandy shoreline curves around a quiet bay beneath broad ochre and sage headlands.",
  ["#4d7c7d", "#7d9a8c", "#294f51"],
  shape(
    coast,
    "none",
    'stroke="#c2c6a5" stroke-opacity=".16" stroke-width="64"',
  ) +
    shape(coast, "#b4ac83", 'stroke="#ded0a3" stroke-width="8"') +
    shape(
      "M0 0H1800V177Q1550 154 1390 286T1110 354Q920 410 952 562T967 714Q694 731 560 908T325 1100H0Z",
      "#899978",
    ) +
    shape("M0 0H1800V95Q1510 82 1290 235T860 300 494 481 0 500Z", "#758d70") +
    line(
      "M1280 317Q1140 332 1050 425M1300 340Q1167 363 1097 437M596 835Q712 726 870 757M608 867Q725 760 849 788",
      0.4,
    ) +
    line(
      "M1290 606Q1440 501 1690 543M1295 636Q1470 544 1640 579M818 1000Q995 904 1220 957",
      0.19,
    ) +
    shape("M0 815Q236 730 430 850L322 1100H0Z", "#607d62"),
);

// V. Valley: a river ribbon between two gently folded slopes.
plate(
  "ael-valley",
  "The Ael Valley",
  "A silver-green river meanders through broad, open slopes toward a distant saddle of hills.",
  ["#8f9e7d", "#a4af91", "#365b49"],
  shape(
    "M0 290Q300 138 510 294T1000 336Q1390 113 1800 292V1100H0Z",
    "#849a7d",
  ) +
    shape(
      "M0 465Q295 275 598 431L962 569Q1340 260 1800 429V1100H0Z",
      "#698969",
    ) +
    shape("M0 748Q410 480 775 623T1270 620 1800 535V1100H0Z", "#9baa81") +
    shape(
      "M1090 370C930 480 1250 563 1030 652S877 786 1030 865 950 1005 783 1100H1090C1290 968 1270 871 1123 806S1190 710 1200 635 1030 484 1110 370Z",
      "#678e87",
    ) +
    line(
      "M1090 370C930 480 1250 563 1030 652S877 786 1030 865 950 1005 783 1100",
      0.5,
      3,
    ) +
    shape("M0 842Q250 616 580 737T832 1100H0Z", "#557a58") +
    shape("M1800 620Q1510 508 1322 691T1395 1100H1800Z", "#567b5e") +
    line(
      "M1360 818Q1520 651 1800 747M1400 850Q1555 711 1770 784M70 879Q297 708 529 808M111 916Q326 765 497 847",
      0.25,
    ) +
    trees(
      [
        [1510, 722, 65],
        [1560, 701, 80],
        [1630, 715, 95],
        [1675, 733, 60],
      ],
      "#416951",
    ),
);

// VI. Frontier: the quietest panel, low horizons and a single winding trail.
plate(
  "orren-plain",
  "The Orren Plain",
  "Low, wind-shaped ridges cross an open grassland, with one faint trail disappearing into the distance.",
  ["#a2a07b", "#bbb697", "#4c6852"],
  shape(
    "M0 345Q225 307 495 373T1030 352 1470 334 1800 362V1100H0Z",
    "#929d7d",
  ) +
    shape("M0 536Q353 390 719 515T1310 495 1800 445V1100H0Z", "#a8aa80") +
    shape(
      "M0 697Q259 568 519 671T1035 636 1530 609 1800 655V1100H0Z",
      "#879974",
    ) +
    shape("M0 934Q358 719 774 869T1340 828 1800 757V1100H0Z", "#708965") +
    line(
      "M1370 440C1190 511 1360 573 1190 668S1090 802 1215 903 1100 1020 1040 1100",
      0.42,
      3,
    ) +
    line(
      "M1015 770Q1230 667 1530 729M1040 802Q1260 707 1500 758M110 651Q325 586 464 639",
      0.2,
    ) +
    shape("M0 1070Q302 975 575 1035T1150 1060 1800 1015V1100H0Z", "#526f54"),
);
