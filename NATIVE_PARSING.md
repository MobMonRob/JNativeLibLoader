Parser
kaitai-struct-compiler 0.9 \
https://kaitai.io/ \
https://doc.kaitai.io/user_guide.html

The original parsing files have been expanded to parse the dependency names.


Parsing dll files
CFF Explorer helped to test https://ntcore.com/?page_id=388


.so format parsing sources
(As of July 2021)

https://github.com/kaitai-io/kaitai_struct_formats/blob/544392ff2c0ddc5afc72218cb3a0833fabc6b63e/executable/elf.ksy

https://github.com/kaitai-io/kaitai_struct_formats/commit/544392ff2c0ddc5afc72218cb3a0833fabc6b63e#diff-75c35e82730c2b8d5270b2b7f30be04aee2db3e4c4aa3a35883b328ebfda6656

https://doc.kaitai.io/user_guide.html

https://greek0.net/elf.html

https://wiki.osdev.org/Dynamic_Linker

https://wiki.osdev.org/ELF_Tutorial#The_String_Table

http://www.skyfree.org/linux/references/ELF_Format.pdf


.dll format parsing sources
(As of July 2021)

RVA umrechnen: https://translate.google.com/translate?sl=ru&tl=en&u=https://habr.com/en/post/129241/

Katai Doku: https://doc.kaitai.io/user_guide.html

PE Übersicht: https://wiki.osdev.org/PE

COFF Übersicht: https://wiki.osdev.org/COFF

COFF Spec: https://www.ti.com/lit/an/spraao8/spraao8.pdf ; http://www.skyfree.org/linux/references/
coff.pdf

PE Anordnung: https://upload.wikimedia.org/wikipedia/commons/1/1b/Portable_Executable_32_bit_Structure_in_SVG_fixed.svg

Test Lib: https://github.com/cubiclesoft/windows-pe-artifact-library

Lib Vergleich: https://lucasg.github.io/2017/04/28/the-sad-state-of-pe-parsing/

Ref Lib: https://github.com/serge1/COFFI/blob/master/coffi/coffi_headers.hpp

Ref Header: https://github.com/trailofbits/pe-parse/blob/master/pe-parser-library/include/pe-parse/nt-headers.h

Lib Übersicht: https://blog.kowalczyk.info/article/65db085cd90f45d2a2718f19a7566e1b/pe-format-pefile.html

PE/COFF Header: https://github.com/germix/coff-explorer/blob/master/proj/src/coff.h

PE Format Doc: https://docs.microsoft.com/en-us/windows/win32/debug/pe-format

Erklärung zu RVA und Import: https://stackoverflow.com/questions/15960437/how-to-read-import-directory-table-in-c/17457077#17457077

PE Format Übersicht: https://docs.microsoft.com/en-us/archive/msdn-magazine/2002/march/inside-windows-an-in-depth-look-into-the-win32-portable-executable-file-format-part-2

Erklarung zu RVA und Import: http://www.sunshine2k.de/reversing/tuts/tut_rvait.htm ;; https://web.archive.org/web/20210706104723/http://www.sunshine2k.de/reversing/tuts/tut_rvait.htm ;; https://archive.fo/jzn2c

Technische Info zu PE: https://web.archive.org/web/20170530071353/https://net.pku.edu.cn/~course/cs201/2003/mirrorWebster.cs.ucr.edu/Page_TechDocs/pe.txt

WINNT Header: https://github.com/wine-mirror/wine/blob/master/include/winnt.h#L2920

Rust PE Header: https://docs.rs/pelite/0.9.0/pelite/image/index.html

Katai PE Seite: https://formats.kaitai.io/microsoft_pe/index.html

NT Header: https://github.com/trailofbits/pe-parse/blob/v1.3.0/pe-parser-library/include/pe-parse/nt-headers.h

NT Header: https://github.com/ziglang/zig/blob/master/lib/libc/include/any-windows-any/ddk/ntimage.h

WINNT Docs: https://docs.microsoft.com/de-de/windows/win32/api/winnt/ns-winnt-image_optional_header64

Example PE Import Parser: http://www.rohitab.com/discuss/topic/38591-c-import-table-parser/

Python PE Parser: https://axcheron.github.io/pe-format-manipulation-with-pefile/

C Import Parser Example: https://gist.github.com/mrexodia/1f9c5aa6570f6c782194

PE Format Erklärung: https://web.archive.org/web/20201109021646/http://www.skynet.ie/~caolan/pub/winresdump/winresdump/doc/pefile.html

PE Visualization: https://web.archive.org/web/20160101153623/http://corkami.googlecode.com/files/PE101-v20L.pdf ;; https://code.google.com/archive/p/corkami/wikis/PE101.wiki

PE Format Erklärung: https://web.archive.org/web/20210507012722/https://www.ired.team/miscellaneous-reversing-forensics/windows-kernel-internals/pe-file-header-parser-in-c++

PE Import Erklärung: https://web.archive.org/web/20210324143651/http://sandsprite.com/CodeStuff/Understanding_imports.html

PE Format Walkthrough: https://web.archive.org/web/20210706111641/https://doc-0o-bc-docs.googleusercontent.com/docs/securesc/ha0ro937gcuc7l7deffksulhg5h7mbp1/u90u77p5u6t36qfgbsnb0b1nvqf0su7b/1625570175000/12563789128225491943/*/0B3_wGJkuWLytQmc2di0wajB1Xzg?e=download

PE Format Layout: https://web.archive.org/web/20210706111833/https://doc-0k-bc-docs.googleusercontent.com/docs/securesc/ha0ro937gcuc7l7deffksulhg5h7mbp1/2q5qhq6u9rtpq2ioicqe9lrvqp8lrnv2/1625570250000/12563789128225491943/*/0B3_wGJkuWLytbnIxY1J5WUs4MEk?e=download

