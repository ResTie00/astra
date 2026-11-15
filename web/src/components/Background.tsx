import myBgImage from '../mars.jpg'; 

export function Background() {
  return (
    <div aria-hidden className="pointer-events-none fixed inset-0 z-0 overflow-hidden bg-black"
    style={{ backgroundImage: `url(${myBgImage})` , backgroundSize:"cover"}}>
      <div className="absolute inset-0 bg-black/40" />
    </div>
  );
}
